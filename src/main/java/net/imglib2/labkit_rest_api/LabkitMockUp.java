package net.imglib2.labkit_rest_api;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvSource;
import bdv.util.volatiles.VolatileViews;
import ij.IJ;
import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.ilastik_mock_up.IlastikMockUpServer;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.inputimage.DefaultInputImage;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.util.Intervals;
import net.imglib2.util.ValuePair;
import org.scijava.Context;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;

public class LabkitMockUp {

	public static void main(String... args) {
		try(IlastikMockUpServer server = new IlastikMockUpServer()) {
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
		Img<ShortType> result = createCacheImage(image, cell -> segmenter.segment(image, cell));
		show(image, result);
	}

	public static Img<ShortType> createCacheImage(Dimensions size, CellLoader<ShortType> loader) {
		return new DiskCachedCellImgFactory<>(new ShortType())
					.create(Intervals.dimensionsAsLongArray(size), loader);
	}

	public static void show(Img<?> image, Img<?> segmentation) {
		BdvSource imageSource = BdvFunctions.show(image, "image");
		imageSource.setDisplayRange(0, 255);
		BdvSource segmentationSource = BdvFunctions.show(VolatileViews.wrapAsVolatile(segmentation),
				"segmentation",
				Bdv.options().addTo(imageSource.getBdvHandle()));
		segmentationSource.setColor(new ARGBType(Color.blue.getRGB()));
		segmentationSource.setDisplayRange(0, 1);
	}
}
