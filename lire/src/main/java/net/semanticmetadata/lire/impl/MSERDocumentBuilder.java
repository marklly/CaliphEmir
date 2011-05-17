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
package net.semanticmetadata.lire.impl;

import ij.ImagePlus;
import ij.process.ImageConverter;
import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.mser.MSER;
import net.semanticmetadata.lire.imageanalysis.mser.MSERFeature;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.RescaleOp;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Creates a Document out of the given image with MSER Features
 * Date: 27.03.2011
 * Time: 10:00:08
 *
 * @author Christine Keim, christine.keim@inode.at
 */
public class MSERDocumentBuilder extends AbstractDocumentBuilder
{
    static ColorSpace cs;
    static ColorConvertOp op;
    static RescaleOp rop;

    static {
        cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        op = new ColorConvertOp(cs, null);
        rop = new RescaleOp(-1.0f, 255f, null);
    }

    private Logger logger = Logger.getLogger(getClass().getName());
    private MSER extractor;

    public MSERDocumentBuilder()
    {
        extractor = new MSER();
    }

    public Document createDocument(BufferedImage image, String identifier)
    {
        Document doc = null;
        try
        {
            // convert to grey ...
            ImagePlus imgPlus = new ImagePlus(identifier, image);
            ImageConverter iconv = new ImageConverter(imgPlus);
            iconv.convertToGray8();
            // extract features from image:
            List<MSERFeature> features = extractor.computeMSERFeatures(imgPlus.getBufferedImage());

            // invert grey
            features.addAll(extractor.computeMSERFeatures(rop.filter(imgPlus.getBufferedImage(), null)));



            // create new document:
            doc = new Document();
            if (features.size()<1) {
                System.err.println("No MSER features found for " + identifier);
            }
            for (Iterator<MSERFeature> fit = features.iterator(); fit.hasNext();)
            {
                MSERFeature f = fit.next();
                boolean skip = false;
                // add each feature to the document:
                // check first if NaN!!
                for (int j = 0; j < f.descriptor.length; j++) {
                    if (Float.isNaN(f.descriptor[j])) skip = true;
                    break;
                }

                if (!skip) doc.add(new Field(DocumentBuilder.FIELD_NAME_MSER, f.getByteArrayRepresentation(), Field.Store.YES));
                else System.err.println("Found NaN in features in file " + identifier + ". ");
            }
            if (identifier != null)
            {
                doc.add(new Field(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES, Field.Index.NOT_ANALYZED));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
//            logger.severe(e.getMessage());
        }
        /*
        catch (IOException e)
        {
            logger.severe(e.getMessage());
        }
        */
        return doc;
    }
}
