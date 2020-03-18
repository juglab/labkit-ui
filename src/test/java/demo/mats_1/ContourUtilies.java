package demo.mats_1;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Utility class to calculate the contour of an binary image.
 */
public class ContourUtilies {

    /**
     * Expects a binary image as input.
     *
     * @return the coordinates of all the
     * pixels at the border between black and white.
     */
    public float[][] getContourPixels(ImagePlus msi) {
        ImagePlus imp_c = getContourImg(msi);
        IJ.run(imp_c, "Analyze Particles...", "size=0-Infinity show=Masks add in_situ");

        Roi ROI_contour_PGI = imp_c.getRoi();
        RoiManager roim = RoiManager.getRoiManager();

        int N_rois = roim.getCount();
        float[] arX_conc =  new float[]{};
        float[] arY_conc =  new float[]{};
        for (int i = 0; i < N_rois; i++) {
            Roi roi = roim.getRoi(i);
            float[] xx = roi.getInterpolatedPolygon().xpoints;
            float[] yy = roi.getInterpolatedPolygon().ypoints;
            arX_conc = ArrayUtils.addAll(arX_conc,xx);
            arY_conc = ArrayUtils.addAll(arY_conc,yy);
        }
        roim.reset();

        return new float[][]{arX_conc, arY_conc};
    }

    public ImagePlus getContourImg(ImagePlus imp) {
        IJ.run(imp, "Outline", "=");
        int width = imp.getWidth();
        int height = imp.getHeight();

        // make an image processor
        ImageProcessor ip = imp.getProcessor();


        for (int x = 0; x < width; x++) {
            ip.putPixel(x, 0, 0);// top edge
            ip.putPixel(x, height - 1, 0); // bottom edge
        }
        for (int y = 0; y < height; y++) {
            ip.putPixel(0, y, 0);// left and right
            ip.putPixel(width - 1, y, 0);
        }
        return imp;
    }
}
