/*
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net.
 *
 * Caliph & Emir is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Caliph & Emir is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Caliph & Emir; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2010 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://www.SemanticMetadata.net
 */

package net.semanticmetadata.lire.imageanalysis.visualattention;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Implements a visual attention model described in F. W. M. Stentiford, <i>An estimator for
 * visual attention through competitive novelty with application to image compression</i>,
 * Proc. Picture Coding Symposium, pp 101-104, Seoul, 24-27 April, 2001.
 *
 * @author Mathias Lux, mathias@juggle.at, http://www.semanticmetadata.net
 *         Date: 22.03.2010
 *         Time: 15:52:24
 */
public class StentifordModel {
    // taken from paper
    private int neighbourhoodSize = 3;
    private int maxChecks = 100;
    // What neighbourhood is deemed similar? ... depends on the color space (heuristic value).
    private int maxDist = 40;

    // stores the random neighbourhood per pixel.
    private HashSet<Integer> randomNeighbourhood = new HashSet<Integer>(neighbourhoodSize);

    // that's the max norm radius we select our neighbours from (heuristic value).
    static int radius = 2;
    static int[][] possibleNeighbours;
    private int[][] attentionModel;

    static {
        int side = 2*radius+1;
        possibleNeighbours = new int[side*side-1][2];
        int count = 0;
        for (int i = -radius; i<=radius; i++) {
            for (int j = -radius; j<=radius; j++) {
                if (j!=0 || i!=0) {
                    possibleNeighbours[count][0] = i;
                    possibleNeighbours[count][1] = j;
                    count++;
                }
            }
        }
    }

    public void extract(BufferedImage img) {
        // temp vars:
        int[][] nx = new int[neighbourhoodSize][3];
        int[][] ny = new int[neighbourhoodSize][3];
        boolean match = true;
        // create attention model array:
        attentionModel = new int[img.getWidth()][img.getHeight()];
        for (int i = 0; i < attentionModel.length; i++) {
            for (int j = 0; j < attentionModel[i].length; j++) {
                attentionModel[i][j] = 0;
            }
        }

        // start with the extraction:
        WritableRaster raster = img.getRaster();
        // for each pixel ...
        for (int x = radius; x < raster.getWidth() - radius; x++) {
            for (int y = radius; y < raster.getHeight() - radius; y++) {
                createRandomNeighbourhood();
                getNeighbourhood(x, y, nx, raster);
                for (int checks = 0; checks<maxChecks; checks++) {
                    getNeighbourhood((int) (Math.random()*(img.getWidth()-2*radius)+radius), 
                            (int) (Math.random()*(img.getHeight()-2*radius)+radius), ny, raster);
                    match = true;
                    for (int i = 0; i < nx.length; i++) {
                        if (getDistance(nx[i], ny[i])>maxDist) {
                            match = false;
                            break;
                        }
                    }
                    if (!match) {
                        attentionModel[x][y]++;
                    }
                }
            }
        }
        System.out.println("");

    }

    /**
     * Get L1 distance between two pixel arrays. Can be used to get the distance between colors
     * or the city-block distance between pixels ;)
     *
     * @param p1
     * @param p2
     * @return
     */
    private int getDistance(int[] p1, int[] p2) {
        int sum = 0;
        for (int i = 0; i < p1.length; i++) {
            sum += Math.abs(p1[i] - p2[i]);
        }
        return sum;
    }

    private void getNeighbourhood(int x, int y, int[][] values, WritableRaster raster) {
        int k = 0;
        for (Iterator<Integer> integerIterator = randomNeighbourhood.iterator(); integerIterator.hasNext();) {
            int n = integerIterator.next();
            raster.getPixel(x+possibleNeighbours[n][0], y+possibleNeighbours[n][1], values[k]);
            // convert to HSV:
            rgb2hsv(values[k][0], values[k][1], values[k][2], values[k]);
            k++;
        }
    }

    private void createRandomNeighbourhood() {
        randomNeighbourhood.clear();
        while (randomNeighbourhood.size()<neighbourhoodSize) {
            int n = (int) (Math.random()*possibleNeighbours.length);
            if (n==possibleNeighbours.length) n--;
            randomNeighbourhood.add(n);
        }
    }

    public int[][] getAttentionModel() {
        return attentionModel;
    }

    /**
     * Visualizes the attention model in a picture. Lighter pixels are the ones with more attention.
     * @return an image visualizing the attention model
     */
    public BufferedImage getAttentionVisualization() {
        BufferedImage result = new BufferedImage(attentionModel.length, attentionModel[0].length, BufferedImage.TYPE_INT_RGB);
        int[] pixel = new int[3];
        for (int i = 0; i < attentionModel.length; i++) {
            for (int j = 0; j < attentionModel[i].length; j++) {
                pixel[0] = (int) (((float) attentionModel[i][j])/((float) maxChecks)*255f);
                pixel[1] = (int) (((float) attentionModel[i][j])/((float) maxChecks)*255f);
                pixel[2] = (int) (((float) attentionModel[i][j])/((float) maxChecks)*255f);
                result.getRaster().setPixel(i,j,pixel);
            }
        }
        return result;
    }

    public static void rgb2hsv(int r, int g, int b, int hsv[]) {
        int min;    //Min. value of RGB
        int max;    //Max. value of RGB
        int delMax; //Delta RGB value

        min = Math.min(r, g);
        min = Math.min(min, b);

        max = Math.max(r, g);
        max = Math.max(max, b);

        delMax = max - min;

//        System.out.println("hsv = " + hsv[0] + ", " + hsv[1] + ", "  + hsv[2]);

        float H = 0f, S = 0f;
        float V = max / 255f;

        if (delMax == 0) {
            H = 0f;
            S = 0f;
        } else {
            S = delMax / 255f;
            if (r == max) {
                if (g >= b) {
                    H = ((g / 255f - b / 255f) / (float) delMax / 255f) * 60;
                } else {
                    H = ((g / 255f - b / 255f) / (float) delMax / 255f) * 60 + 360;
                }
            } else if (g == max) {
                H = (2 + (b / 255f - r / 255f) / (float) delMax / 255f) * 60;
            } else if (b == max) {
                H = (4 + (r / 255f - g / 255f) / (float) delMax / 255f) * 60;
            }
        }
//        System.out.println("H = " + H);
        hsv[0] = (int) (H);
        hsv[1] = (int) (S * 100);
        hsv[2] = (int) (V * 100);
    }

}
