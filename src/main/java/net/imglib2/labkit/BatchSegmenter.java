
package net.imglib2.labkit;

import ij.ImagePlus;
import io.scif.img.ImgSaver;
import net.imagej.ImgPlus;
import net.imglib2.img.VirtualStackAdapter;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import bdv.export.ProgressWriter;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;

import java.io.File;

/**
 * Helper class for segmenting multiple image files.
 *
 * @author Matthias Arzt
 */
public class BatchSegmenter {

	private final ImgSaver saver = new ImgSaver();
	private final Segmenter segmenter;
	private final ProgressWriter progressWriter;

	public BatchSegmenter(Segmenter segmenter, ProgressWriter progressWriter) {
		this.segmenter = segmenter;
		this.progressWriter = progressWriter;
	}

	public void segment(File inputFile, File outputFile) {
		ImgPlus<?> img = VirtualStackAdapter.wrap(new ImagePlus(inputFile.getAbsolutePath()));
		Img<UnsignedByteType> result = ArrayImgs.unsignedBytes(Intervals.dimensionsAsLongArray(img));
		segmenter.segment(img, result);
		saver.saveImg(outputFile.getAbsolutePath(), result);
	}

}
