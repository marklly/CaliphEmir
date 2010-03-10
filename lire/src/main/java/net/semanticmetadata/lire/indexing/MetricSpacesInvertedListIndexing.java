/*
 * This file is part of Lire (Lucene Image Retrieval).
 *
 * Lire is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Lire is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Lire; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2008 by Mathias Lux (mathias@juggle.at)
 * http://www.semanticmetadata.net
 */
package net.semanticmetadata.lire.indexing;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.impl.GenericImageSearcher;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocCollector;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;


/**
 * This class provides an indexing approach for approximate search based on the work of G. Amato
 * (giuseppe.amato@isti.cnr.it). See also his paper "Approximate Similarity Search in Metric Spaces
 * using Inverted Files"
 * Date: 14.05.2009
 * Time: 14:22:03
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class MetricSpacesInvertedListIndexing {
    public static int numReferenceObjects = 500;
    public static int numReferenceObjectsUsed = 100;

    private static MetricSpacesInvertedListIndexing msili = new MetricSpacesInvertedListIndexing(CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);

    private Class featureClass;
    private String featureFieldName;
    private int numHits = 100;

    private ProgressIndicator progress;

    public enum State {
        RoSelection, RoIndexing, Indexing, Idle
    }

    ;

    /**
     * @param featureClass     the feature being used for this new index (e.g. CEDD)
     * @param featureFieldName the field name where to find the feature.
     */
    public MetricSpacesInvertedListIndexing(Class featureClass, String featureFieldName) {
        this.featureClass = featureClass;
        this.featureFieldName = featureFieldName;
        progress = new ProgressIndicator();
    }

    public static MetricSpacesInvertedListIndexing getDefaultInstance() {
        return msili;
    }

    /**
     * Creates a set of reference objects and stores it in a new index (name "<indexPath>-ro"). Then creates ordered
     * lists of reference object positions for each data item in the index with given feature.
     * Finally a new index (name "<indexPath>-ms") is created where all the original documents as well as the new data
     * are stored.
     *
     * @param indexPath the path to the original index
     * @throws IOException
     */
    public void createIndex(String indexPath) throws IOException {
        IndexReader reader = IndexReader.open(indexPath);
        int numDocs = reader.numDocs();

        if (numDocs < numReferenceObjects) {
            throw new UnsupportedOperationException("Too few documents in index.");
        }

        // progress report
        progress.setNumDocsAll(numDocs);
        progress.setCurrentState(State.RoSelection);

        boolean hasDeletions = reader.hasDeletions();

        // init reference objects:
        IndexWriter iw = new IndexWriter(indexPath + "-ro", new SimpleAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
        HashSet<Integer> referenceObjsIds = new HashSet<Integer>(numReferenceObjects);

        double numDocsDouble = (double) numDocs;
        while (referenceObjsIds.size() < numReferenceObjects) {
            referenceObjsIds.add((int) (numDocsDouble * Math.random()));
        }
        int count = 0;

        if (hasDeletions) {
            System.err.println("WARNING: There are deleted docs in your index. You should " +
                    "optimize your index before using this method.");
        }

        // progress report
        progress.setCurrentState(State.RoIndexing);

        // find them in the index and put them into a separate index:
        for (int i : referenceObjsIds) {
            count++;
            Document document = reader.document(i);
            document.add(new Field("ro-id", count + "", Field.Store.YES, Field.Index.NOT_ANALYZED));
            iw.addDocument(document);
        }
        iw.optimize();
        iw.close();

        // progress report
        progress.setCurrentState(State.Indexing);

        // now find the reference objects for each entry ;)
        IndexReader readerRo = IndexReader.open(indexPath + "-ro");
        ImageSearcher searcher = new GenericImageSearcher(numReferenceObjectsUsed, featureClass, featureFieldName);
        PerFieldAnalyzerWrapper aWrapper =
                new PerFieldAnalyzerWrapper(new SimpleAnalyzer());
        aWrapper.addAnalyzer("ro-order", new WhitespaceAnalyzer());

        iw = new IndexWriter(indexPath, aWrapper, false, IndexWriter.MaxFieldLength.UNLIMITED);
        StringBuilder sb = new StringBuilder(256);
        for (int i = 0; i < numDocs; i++) {
            if (hasDeletions && reader.isDeleted(i)) {
                continue;
            }
            Document document = reader.document(i);
            ImageSearchHits hits = searcher.search(document, readerRo);
            sb.delete(0, sb.length());
            for (int j = 0; j < numReferenceObjectsUsed; j++) {
                sb.append(hits.doc(j).getValues("ro-id")[0]);
                sb.append(' ');
            }
            // System.out.println(sb.toString());
            document.add(new Field("ro-order", sb.toString(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
            iw.updateDocument(new Term(DocumentBuilder.FIELD_NAME_IDENTIFIER, document.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]), document);

            // progress report
            progress.setNumDocsProcessed(progress.getNumDocsProcessed() + 1);

        }
        iw.optimize();
        iw.close();

        // progress report
        progress.setCurrentState(State.Idle);

    }

    /**
     * We assume that the initial indexing has been done and a set of reference objects has been
     * found and indexed in the sepaarete directory. However further documents were added and they
     * now need to get a ranked list of reference objects. So we (i) get all these new documents
     * missing the field "ro-order" and (ii) add this field.
     *
     * @param indexPath the index to update
     * @throws IOException
     */
    public void updateIndex(String indexPath) throws IOException {
        IndexReader reader = IndexReader.open(indexPath);
        int numDocs = reader.numDocs();
        boolean hasDeletions = reader.hasDeletions();
        int countUpdated = 0;

        IndexReader readerRo = IndexReader.open(indexPath + "-ro");
        ImageSearcher searcher = new GenericImageSearcher(numReferenceObjectsUsed, featureClass, featureFieldName);
        PerFieldAnalyzerWrapper aWrapper =
                new PerFieldAnalyzerWrapper(new SimpleAnalyzer());
        aWrapper.addAnalyzer("ro-order", new WhitespaceAnalyzer());

        IndexWriter iw = new IndexWriter(indexPath, aWrapper, false, IndexWriter.MaxFieldLength.UNLIMITED);
        StringBuilder sb = new StringBuilder(256);
        for (int i = 0; i < numDocs; i++) {
            if (hasDeletions && reader.isDeleted(i)) {
                continue;
            }
            Document document = reader.document(i);
            if (document.getField("ro-order") == null) {  // if the field is not here we create it.
                ImageSearchHits hits = searcher.search(document, readerRo);
                sb.delete(0, sb.length());
                for (int j = 0; j < numReferenceObjectsUsed; j++) {
                    sb.append(hits.doc(j).getValues("ro-id")[0]);
                    sb.append(' ');
                }
                // System.out.println(sb.toString());
                document.add(new Field("ro-order", sb.toString(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
                iw.updateDocument(new Term(DocumentBuilder.FIELD_NAME_IDENTIFIER, document.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]), document);
                countUpdated++;
            }

            // progress report
            progress.setNumDocsProcessed(progress.getNumDocsProcessed() + 1);

            // debug:
            System.out.println("countUpdated = " + countUpdated);
        }
        iw.optimize();
        iw.close();
    }

    /**
     * Provides basic search functions ...
     *
     * @param img
     * @param indexPath
     * @return
     * @throws IOException
     */
    public TopDocs search(BufferedImage img, String indexPath) throws IOException {
        ImageSearcher searcher = new GenericImageSearcher(numReferenceObjectsUsed, featureClass, featureFieldName);
        ImageSearchHits hits = searcher.search(img, IndexReader.open(indexPath + "-ro"));
        StringBuilder sb = new StringBuilder(numReferenceObjectsUsed * 4);
        for (int j = 0; j < numReferenceObjectsUsed; j++) {
            sb.append(hits.doc(j).getValues("ro-id")[0]);
            sb.append(' ');
        }
        return scoreDocs(sb.toString(), IndexReader.open(indexPath));
    }

    /**
     * Provides basic search functions ...
     *
     * @param d
     * @param indexPath
     * @return
     * @throws IOException
     */
    public TopDocs search(Document d, String indexPath) throws IOException {
        if (d.getField("ro-order") != null) // if the document already contains the information on reference object neighbourhood
            return scoreDocs(d.getValues("ro-order")[0], IndexReader.open(indexPath));
        else { // if not we just create it :)
            ImageSearcher searcher = new GenericImageSearcher(numReferenceObjectsUsed, featureClass, featureFieldName);
            ImageSearchHits hits = searcher.search(d, IndexReader.open(indexPath + "-ro"));
            StringBuilder sb = new StringBuilder(numReferenceObjectsUsed * 4);
            for (int j = 0; j < numReferenceObjectsUsed; j++) {
                sb.append(hits.doc(j).getValues("ro-id")[0]);
                sb.append(' ');
            }
            return scoreDocs(sb.toString(), IndexReader.open(indexPath));
        }
    }

    /**
     * Scoring function based on the footrule distance.
     *
     * @param queryString
     * @param reader
     * @return
     * @throws IOException
     */
    protected TopDocs scoreDocs(String queryString, IndexReader reader) throws IOException {
        // TODO: optimize here ;) Perhaps focus on the most promising results
        StringTokenizer st = new StringTokenizer(queryString);
        int position = 0;
        HashMap<Integer, Integer> doc2score = new HashMap<Integer, Integer>(1000);
        HashMap<Integer, Integer> doc2count = new HashMap<Integer, Integer>(1000);
        int currDoc = 0;
        while (st.hasMoreTokens()) {
            TermPositions tp = reader.termPositions(new Term("ro-order", st.nextToken()));
            while (tp.next()) {
                currDoc = tp.doc();
                // System.out.println(tp.doc() + ": " + tp.nextPosition());
                if (doc2score.get(currDoc) == null) {
                    doc2score.put(currDoc, Math.abs(tp.nextPosition() - position));
                    doc2count.put(currDoc, 1);
                } else {
                    doc2score.put(currDoc, doc2score.get(currDoc) + Math.abs(tp.nextPosition() - position));
                    doc2count.put(currDoc, doc2count.get(currDoc) + 1);
                }

            }
            position++;
        }
        // fill up all the remaining doc scores,
        TopFieldDocCollector col = new TopFieldDocCollector(reader, Sort.RELEVANCE, numHits);
        for (Iterator<Integer> iterator = doc2count.keySet().iterator(); iterator.hasNext();) {
            currDoc = iterator.next();
//            if (doc2count.get(currDoc) < position) {
//                doc2score.put(currDoc, doc2score.get(currDoc) + (numRefObjs-doc2count.get(currDoc))*position);
            // high score: relevant
            col.collect(currDoc, (position + 1) * position - (doc2score.get(currDoc) + (position - doc2count.get(currDoc)) * position));
            // low score: relevant
            // col.collect(currDoc, (doc2score.get(currDoc) + (position - doc2count.get(currDoc)) * position));
//            } else {
//                col.collect(currDoc, (position + 1) * position - (doc2score.get(currDoc) + (position - doc2count.get(currDoc)) * position));
//            }
        }
        return col.topDocs();
    }

    public int getNumHits() {
        return numHits;
    }

    public void setNumHits(int numHits) {
        this.numHits = numHits;
    }

    /**
     * Returns a reader for the index consisting the documents with the approximate search information.
     *
     * @param indexPath
     * @return
     * @throws IOException
     */
    public IndexReader getIndexReader(String indexPath) throws IOException {
        return IndexReader.open(indexPath);
    }

    public ProgressIndicator getProgress() {
        return progress;
    }

    public void setProgress(ProgressIndicator progress) {
        this.progress = progress;
    }


    // ******************************************************************************
    // ** Inner class ...
    // ******************************************************************************

}
