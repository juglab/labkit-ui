package demo.mats_1;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ChannelSplitter;
import net.imglib2.img.VirtualStackAdapter;
import net.imglib2.type.numeric.ARGBType;

public class BiisSegmenter {

	public static void run(ImagePlus input_img_org, ImagePlus input_img_msi_org) {
		// render the input image to 8-bit once.
		ImagePlus input_image = input_img_org.duplicate();
		ImagePlus manual_segmented_image = input_img_msi_org.duplicate();

		IJ.run(input_image, "8-bit", "");

		ImagePlus[] channels = ChannelSplitter.split(input_image);
		input_image = channels[0];
		IJ.run(input_image, "Grays", "");

		Pipeline_fMN_fast pipe_fast = new Pipeline_fMN_fast(input_image);
		Likelihood likelihood = new Likelihood(pipe_fast,
				manual_segmented_image);
		float[] result = new Optimizer_v10_img_fastPipe().optimise(likelihood);

		ImagePlus pgi = pipe_fast.exec(result); // pipeline generated image

		BdvStackSource< ? > handle = BdvFunctions.show(VirtualStackAdapter.wrap(
				input_image), input_image.getTitle(), BdvOptions.options().is2D());
		BdvFunctions.show(VirtualStackAdapter.wrap(pgi), input_image.getTitle(), BdvOptions.options().addTo(handle.getBdvHandle())).setColor(new ARGBType(0x770000));
	}
}
