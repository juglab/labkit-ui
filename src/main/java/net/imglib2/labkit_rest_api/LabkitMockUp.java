package net.imglib2.labkit_rest_api;

import bdv.util.BdvFunctions;
import ij.IJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DiskCachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.ilastik_mock_up.Server;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.inputimage.DefaultInputImage;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.util.ValuePair;
import org.scijava.Context;

import java.io.IOException;
import java.util.Collections;

public class LabkitMockUp {

	public static void main(String... args) {
		try(Server server = new Server()) {
			System.out.println("Press any key to exit");
			LabkitMockUp.run();
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void run() {
		Img<?> image = ImageJFunctions.wrap(IJ.openImage("http://imagej.nih.gov/ij/images/t1-head.zip"));
		DefaultInputImage inputImage = new DefaultInputImage((RandomAccessibleInterval<? extends NumericType<?>>) image);
		RestSegmenter segmenter = new RestSegmenter(new Context(), inputImage);
		segmenter.train(Collections.singletonList(new ValuePair<>(image, null)));
		CellLoader<UnsignedByteType> loader = cell -> {
			segmenter.segment(image, cell);
		};
		DiskCachedCellImg<UnsignedByteType, ?> result = new DiskCachedCellImgFactory<>(new UnsignedByteType())
				.create(Intervals.dimensionsAsLongArray(image), loader);
		BdvFunctions.show(result, "result");
	}
}
