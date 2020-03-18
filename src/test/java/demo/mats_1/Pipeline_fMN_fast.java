package demo.mats_1;

import ij.IJ;
import ij.ImagePlus;

public class Pipeline_fMN_fast {

    private final ImagePlus preprocessed_image;

    // This Pipeline is the same as Pipeline. Only this one should be used in the Bayesian RadFriends Optimizer (Optimizer_v5_img.java)

    public Pipeline_fMN_fast(ImagePlus input_image) {
        preprocessed_image = input_image.duplicate();
        IJ.run(preprocessed_image, "Smooth", "");
        IJ.run(preprocessed_image, "Enhance Contrast", "saturated=0.35");
        IJ.run(preprocessed_image, "Apply LUT", "");
        IJ.run(preprocessed_image, "Bandpass Filter...", "filter_large=40 filter_small=3 suppress=None tolerance=5 autoscale saturate");
        IJ.run(preprocessed_image, "Smooth", ""); // 3x3 mean filter
        IJ.run(preprocessed_image, "Find Edges", ""); // calculates gradient magnitude of sobel filters.
    }

    public ImagePlus exec(float[] param) {
        ImagePlus imp = preprocessed_image.duplicate();
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
        IJ.run(imp, "Analyze Particles...", "size="+String.valueOf(minParticle_white)+"-Infinity show=Masks in_situ");
        IJ.run(imp, "Invert", "");
        //IJ.saveAs(imp, "Tiff", "/home/mats/Dokumente/postdoc/conferences/fiji_hackathon_dd2019/new_edge_detection-py/test.tif");
        IJ.run(imp, "Analyze Particles...", "size="+String.valueOf(minParticle_black)+"-Infinity show=Masks in_situ");


        //imp.show();
        //sleep(1000);
        //IJ.run(imp, "8-bit", "");
        return imp;
    }
}
