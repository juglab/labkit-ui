package demo.mats_1;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ChannelSplitter;

public class BiisSegmenter {

	public static ImagePlus run(ImagePlus input_img_org, ImagePlus manual_segmented_image_original) {
		// render the input image to 8-bit once.
		ImagePlus input_image = input_img_org.duplicate();
		ImagePlus manual_segmented_image = manual_segmented_image_original.duplicate();

		IJ.run(input_image, "8-bit", "");

		ImagePlus[] channels = ChannelSplitter.split(input_image);
		input_image = channels[0];
		IJ.run(input_image, "Grays", "");

		Pipeline_fMN_fast pipe_fast = new Pipeline_fMN_fast(input_image);
		BiisOptimizationProblem likelihood = new BiisOptimizationProblem(pipe_fast,
				manual_segmented_image);
		float[] result = new BayesOptimizer(likelihood).run();

		return pipe_fast.exec(result); // pipeline generated image
	}
}
