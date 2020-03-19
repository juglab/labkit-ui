package demo.mats_1;

import ij.ImagePlus;

public class Likelihood {

	private final Pipeline_fMN_fast pipe_fast;

	private final float[][] manual_segmentation_contour;

	public Likelihood(Pipeline_fMN_fast pipe_fast, ImagePlus manual_segmented_image) {
		this.pipe_fast = pipe_fast;
		ContourUtilies pipe_contour = new ContourUtilies();
		this.manual_segmentation_contour = pipe_contour.getContourPixels(manual_segmented_image);
	}

	public int numberOfParameters() {
		return 5;
	}

	public float[][] parameterBounds() {
		return new float[][] {
				//{0.1f,5},   // Cannylow
				//{0.1f,5},	  // Cannyhigh
				{0,100},	// gaussian_radius
				{0,50},	 // Threshold_high
				{0,10},   // minParticle_white
				{0,10},   // minParticle_black

				{0,40}	 // sigma # error of the parameter estimation
		};
	}

	public double likelihood(float[] param) {
		float sigma  = param[4];
		float[] xx_msi= manual_segmentation_contour[0];
		float[] yy_msi= manual_segmentation_contour[1];

		double logLH = 0;
		// now apply the pipeline to an image
		ImagePlus pgi = pipe_fast.exec(param); // pipeline generated image

		// PERHAPS ONE HAS TO TURN THIS ON. NOT SURE!
		//get contour image

		ContourUtilies pipe_contour = new ContourUtilies();


		//get pixels from contour image
		float[][] pgi_contour = pipe_contour.getContourPixels(pgi);
		float[] xx_pgi= pgi_contour[0];
		float[] yy_pgi= pgi_contour[1];
		// Check that the length of contour of the pipeline generate image is not zero:
		if (xx_pgi.length > 10){


			//prepare everything for the nearest neighbour query:

			float[] arx1;
			float[] ary1;
			float[] arx2;
			float[] ary2;

			if (xx_pgi.length >= xx_msi.length) {
				arx1 = xx_pgi;
				ary1 = yy_pgi;
				arx2 = xx_msi;
				ary2 = yy_msi;
			} else {
				arx1 = xx_msi;
				ary1 = yy_msi;
				arx2 = xx_pgi;
				ary2 = yy_pgi;
			}

			PointSet kdtree = new PointSet(arx2, ary2);

			for (int i = 0; i < arx1.length; i = i + 1) {
				float[] testpoint = new float[]{arx1[i], ary1[i]};

				float dist = kdtree.distanceTo(testpoint[0], testpoint[1]); // this comes down to data - model

				logLH += -0.5 * Math.log(2 * Math.PI * Math.pow(sigma, 2)) + -0.5 * (Math.pow(dist, 2) / Math.pow(sigma, 2));

			}
		} else { // in case the parameters make an all black or all white image. ... so if no contours are present, just punish this very bad
			for (int i = 0; i < xx_msi.length; i = i + 1) {
				float dist = pgi.getWidth()+ pgi.getHeight(); // a distance bigger than any distance that could occur in the image.
				logLH += -0.5 * Math.log(2 * Math.PI * Math.pow(sigma, 2)) + -0.5 * (Math.pow(dist, 2) / (Math.pow(sigma, 2)));
			}
		}

		return logLH;
	}
}
