package net.imglib2.atlas.plugin;

import net.imagej.Dataset;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.atlas.MainFrame;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * @author Matthias Arzt
 */
@Plugin(type = Command.class, menuPath = "Plugins > Segmentation > BDV Labkit")
public class TrainableSegmentationRevamp implements Command {

	@Parameter
	Dataset input;

	@Override
	public void run() {
		Img<? extends RealType<?>> img = input.getImgPlus().getImg();
		new MainFrame(RevampUtils.uncheckedCast(img), false);
	}
}
