/*
 * This file is part of the LIRE project: http://www.SemanticMetadata.net/lire.
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
 * Note, that the SIFT-algorithm is protected by U.S. Patent 6,711,293: "Method and
 * apparatus for identifying scale invariant features in an image and use of same for
 * locating an object in an image" by the University of British Columbia. That is, for
 * commercial applications the permission of the author is required.
 *
 * (c) 2008 by Mathias Lux, mathias@juggle.at
 */
package net.semanticmetadata.lire.imageanalysis.sift;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.impl.SiftDocumentBuilder;
import net.semanticmetadata.lire.impl.SiftLocalFeatureHistogramImageSearcher;
import net.semanticmetadata.lire.utils.FileUtils;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ...
 * Date: 23.09.2008
 * Time: 17:26:00
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class TestLocalFeatureHistogram extends TestCase {
    private Extractor extractor;
    private String indexPath = "wang-index";
    private String testExtensive = "./lire/wang-data-1000";
    private int[] sampleQueries = {284, 77, 108, 416, 144, 534, 898, 104, 67, 10, 607, 165, 343, 973, 591, 659, 812, 231, 261, 224, 227, 914, 427, 810, 979, 716, 253, 708, 751, 269, 531, 699, 835, 370, 642, 504, 297, 970, 929, 20, 669, 434, 201, 9, 575, 631, 730, 7, 546, 816, 431, 235, 289, 111, 862, 184, 857, 624, 323, 393, 465, 905, 581, 626, 212, 459, 722, 322, 584, 540, 194, 704, 410, 267, 349, 371, 909, 403, 724, 573, 539, 812, 831, 600, 667, 672, 454, 873, 452, 48, 322, 424, 952, 277, 565, 388, 149, 966, 524, 36, 528, 75, 337, 655, 836, 698, 230, 259, 897, 652, 590, 757, 673, 937, 676, 650, 297, 434, 358, 789, 484, 975, 318, 12, 506, 38, 979, 732, 957, 904, 852, 635, 620, 28, 59, 732, 84, 788, 562, 913, 173, 508, 32, 16, 882, 847, 320, 185, 268, 230, 259, 931, 653, 968, 838, 906, 596, 140, 880, 847, 297, 77, 983, 536, 494, 530, 870, 922, 467, 186, 254, 727, 439, 241, 12, 947, 561, 160, 740, 705, 619, 571, 745, 774, 845, 507, 156, 936, 473, 830, 88, 66, 204, 737, 770, 445, 358, 707, 95, 349};


    protected void setUp() throws Exception {
        super.setUp();
        extractor = new Extractor();
    }

    public void testKMeans() throws IOException {
        ArrayList<String> images = FileUtils.getAllImages(new File(testExtensive), true);
        KMeans k = new KMeans();
        int count = 0;
        for (int i = 0; i < sampleQueries.length; i++) {
            int id = sampleQueries[i];
//            System.out.print("id = " + id + ": ");
            String s = testExtensive + "/" + id + ".jpg";
            System.out.println("s = " + s);
            List<Feature> features = extractor.computeSiftFeatures(ImageIO.read(new File(s)));
            k.addImage(s, features);
            if (count > 20) break;
            count++;
        }
        System.out.println("Init clustering");
        k.init();
        System.out.println("First step");
        double laststress = k.clusteringStep();
        System.out.println("2nd step");
        double newstress = k.clusteringStep();
        while (newstress > laststress) {
            System.out.println("newstress-laststress = " + (newstress - laststress));
            laststress = newstress;
            newstress = k.clusteringStep();
            System.out.print(".");
        }
        System.out.println("\nfinished");
        printClusters(k);

        // create histograms ...
        List<Image> imgs = k.getImages();
        Cluster[] clusters = k.getClusters();
        for (Iterator<Image> imageIterator = imgs.iterator(); imageIterator.hasNext();) {
            Image image = imageIterator.next();
            image.initHistogram(k.getNumClusters());
            for (Iterator<Feature> iterator = image.features.iterator(); iterator.hasNext();) {
                Feature feat = iterator.next();
                image.getLocalFeatureHistogram()[k.getClusterOfFeature(feat)]++;
                image.normalizeFeatureHistogram();
            }
        }

        for (Image i : imgs) {
            i.printHistogram();
        }
    }

    private void printClusters(KMeans k) {
        Cluster[] clusters = k.getClusters();
        for (int i = 0; i < clusters.length; i++) {
            Cluster cluster = clusters[i];
            System.out.println(i + ": " + cluster.toString());
        }
    }

    public void testSiftIndexing() throws IOException {
        SiftDocumentBuilder builder = new SiftDocumentBuilder();
        IndexWriter iw = new IndexWriter("sift-idx", new SimpleAnalyzer(), true);
        for (int i = 0; i < sampleQueries.length; i++) {
            int sampleQuery = sampleQueries[i];
            String s = testExtensive + "/" + sampleQuery + ".jpg";
            iw.addDocument(builder.createDocument(new FileInputStream(s), s));
            if (i % 10 == 0) System.out.print(".");
        }
        System.out.println("");
        iw.optimize();
        iw.close();
    }

    public void testCreateLocalFeatureHistogram() throws IOException {
        SiftFeatureHistogramBuilder sh = new SiftFeatureHistogramBuilder(IndexReader.open("sift-idx"));
        sh.index();
    }

    public void testFindimages() throws IOException {
        IndexReader reader = IndexReader.open("sift-idx");

        SiftLocalFeatureHistogramImageSearcher searcher = new SiftLocalFeatureHistogramImageSearcher(10);
        ImageSearchHits searchHits = searcher.search(reader.document(0), reader);
        for (int i = 0; i < searchHits.length(); i++) {
            Document document = searchHits.doc(i);
            String file = document.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            System.out.println(searchHits.score(i) + ": " + file);
        }
    }
}
