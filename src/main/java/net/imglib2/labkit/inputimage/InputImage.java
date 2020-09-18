
package net.imglib2.labkit.inputimage;

import net.imagej.ImgPlus;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.type.numeric.NumericType;

/**
 * Interface that represents an image as {@link ImgPlus} and
 * {@link BdvShowable}.
 * <p>
 * Labkit does two things with an image, displaying and segmenting. Representing
 * an image as {@link ImgPlus} works best for segmentation. {@link BdvShowable}
 * is required for displaying.
 */
// TODO: Maybe it makes sense to merge BdvShowable and InputImage.
public interface InputImage {

	ImgPlus<? extends NumericType<?>> imageForSegmentation();

	default BdvShowable showable() {
		return BdvShowable.wrap(imageForSegmentation());
	}

	String getDefaultLabelingFilename();
}
