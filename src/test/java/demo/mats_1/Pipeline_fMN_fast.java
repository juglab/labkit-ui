package demo.mats_1;

import ij.IJ;
import ij.ImagePlus;

public class Pipeline_fMN_fast {
    // This Pipeline is the same as Pipeline. Only this one should be used in the Bayesian RadFriends Optimizer (Optimizer_v5_img.java)

    public ImagePlus exec(ImagePlus imp, float[] param) {
        //float Cannyhigh         = param[0];
        //float Cannylow          = param[1];
        float gaussian_radius   = param[0];
        float Threshold_low     = 0f;
        float Threshold_high    = param[1];
        float minParticle_white = param[2];
        float minParticle_black = param[3];



        //if (Cannyhigh <= Cannylow){}

        // convert to 8 bit
        //IJ.run(imp, "8-bit", "");

        // Canny edge detection
        //Canny_Edge_Detector canny = new Canny_Edge_Detector();
        //canny.setHighThreshold(Cannyhigh);
        //canny.setLowThreshold(Cannylow);
        //imp = canny.process(imp);


        // blur
        IJ.run(imp, "Gaussian Blur...", "sigma="+String.valueOf(gaussian_radius));
        // threshold
        IJ.setRawThreshold(imp, Threshold_low, Threshold_high, null);
        IJ.run(imp, "Convert to Mask", "");
        // particles
        /*
        IJ.run(imp, "Analyze Particles...", "size="+String.valueOf(minParticle_white)+"-Infinity show=Masks in_situ");
        IJ.run(imp, "Invert", "");
        IJ.run(imp, "Analyze Particles...", "size="+String.valueOf(minParticle_black)+"-Infinity show=Masks in_situ");
        */


        //imp.show();
        //sleep(1000);
        //IJ.run(imp, "8-bit", "");
        return imp;
    }

    private void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
