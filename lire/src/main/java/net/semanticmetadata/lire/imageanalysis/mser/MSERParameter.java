package net.semanticmetadata.lire.imageanalysis.mser;

/**
 * Created by IntelliJ IDEA.
 * User: Shotty
 * Date: 30.06.2010
 * Time: 15:11:46
 */
public class MSERParameter
{
    // delta, in the code, it compares (size_{i}-size_{i-delta})/size_{i-delta}
     int delta;
     // prune the area which bigger/smaller than maxArea/minArea
     double maxArea;
     double minArea;
     // prune the area have similar size to its children
     double maxVariation;
     // trace back to cut off mser with diversity < min_diversity
     double minDiversity;
    
     /* the next few params for MSER of color image */
     // for color image, the evolution steps
     int maxEvolution;
     // the area threshold to cause re-initialize
     double areaThreshold;
     // ignore too small margin
     double minMargin;
     // the aperture size for edge blur
     int edgeBlurSize;

    /**
     * Constructor with default values
     *
     */
    public MSERParameter()
    {
        // original of paper: 3, 0.5, > 25 pixel, 1, 0.5
        this.delta = 5;
        this.minArea = 0.001;
        this.maxArea = 0.5;
        this.maxVariation = 1;
        this.minDiversity = 0.75F;
//        this.delta = 5;
//        this.minArea = 0.001;
//        this.maxArea = 0.5;
//        this.maxVariation = 1;
//        this.minDiversity = 0.75F;

/*
        this.minArea = 60;
        this.maxArea = 14400;

        this.maxEvolution = 200;
        this.areaThreshold = 1.01;
        this.minMargin = 0.003;
        this.edgeBlurSize = 5;
*/
    }

    public MSERParameter(int delta,
                         double minArea, double maxArea,
                         double maxVariation, double minDiversity,
                         int maxEvolution, double areaThreshold,
                         double minMargin, int edgeBlurSize)
    {
        this.delta = delta;
        this.minArea = minArea;
        this.maxArea = maxArea;
        this.maxVariation = maxVariation;
        this.minDiversity = minDiversity;

/*
        this.maxEvolution = maxEvolution;
        this.areaThreshold = areaThreshold;
        this.minMargin = minMargin;
        this.edgeBlurSize = edgeBlurSize;
*/
    }
}
