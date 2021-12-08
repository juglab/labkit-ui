
package net.imglib2.labkit.models;

import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imagej.patcher.LegacyInjector;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.VirtualStackAdapter;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.labkit.LabkitFrame;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.segmentation.Segmenter;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;
import net.imglib2.type.NativeType;

import java.util.function.Consumer;

/**
 * {@link SegmentationModel} allows to set a cached image factory via extension
 * point. This feature allows to customize how the lazy loaded, cached
 * segmentation and probability maps behave.
 * <p>
 * This class demonstrates on such possible customization. It sets a cached
 * image factory, which for simplicity uses no caching and no lazy loading. It
 * instead calculates the the entire segmentation (or probability map) an stores
 * it into a simple ArrayImg.
 */
public class CustomCachedImageFactoryDemo {

	static {
		LegacyInjector.preinit();
	}

	public static void main(String... args) {
		final ImagePlus imp = new ImagePlus("https://imagej.nih.gov/ij/images/FluorescentCells.jpg");
		ImgPlus<?> image = VirtualStackAdapter.wrap(imp);
		DefaultSegmentationModel segmentationModel = new DefaultSegmentationModel(SingletonContext
			.getInstance(), new DatasetInputImage(image));
		segmentationModel.extensionPoints().setCachedSegmentationImageFactory(
			CustomCachedImageFactoryDemo::noCacheFactory);
		segmentationModel.extensionPoints().setCachedPredictionImageFactory(
			CustomCachedImageFactoryDemo::noCacheFactory);
		LabkitFrame.show(segmentationModel,
			"Custom Cached Image Factory Demo, That actually uses no cahce.");
	}

	private static <T extends NativeType<T>> Img<T> noCacheFactory(Segmenter segmenter,
		Consumer<RandomAccessibleInterval<T>> loader, CellGrid cellGrid, T t)
	{
		Img<T> image = new ArrayImgFactory<>(t).create(cellGrid.getImgDimensions());
		loader.accept(image);
		return image;
	}
}
