
package demo.mats_1;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ChannelSplitter;

public class BiisSegmenter {

	private float[] parameters;

	public void train(ImagePlus image, ImagePlus manualSegmentation) {
		ImagePlus input_image = asUnsignedByteType(image);
		ImagePlus manual_segmented_image = manualSegmentation.duplicate();
		Pipeline_fMN_fast pipe_fast = new Pipeline_fMN_fast(input_image);
		BiisOptimizationProblem likelihood = new BiisOptimizationProblem(pipe_fast,
			manual_segmented_image);
		parameters = new BayesOptimizer(likelihood).run();
	}

	public ImagePlus apply(ImagePlus image) {
		Pipeline_fMN_fast pipe_fast = new Pipeline_fMN_fast(asUnsignedByteType(image));
		return pipe_fast.exec(parameters); // pipeline generated image
	}

	private static ImagePlus asUnsignedByteType(ImagePlus input_img_org) {
		ImagePlus input_image = input_img_org.duplicate();
		IJ.run(input_image, "8-bit", "");
		ImagePlus[] channels = ChannelSplitter.split(input_image);
		input_image = channels[0];
		IJ.run(input_image, "Grays", "");
		return input_image;
	}
}
