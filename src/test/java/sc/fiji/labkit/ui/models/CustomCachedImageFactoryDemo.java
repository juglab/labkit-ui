/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.ui.models;

import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imagej.patcher.LegacyInjector;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.VirtualStackAdapter;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellGrid;
import sc.fiji.labkit.ui.LabkitFrame;
import sc.fiji.labkit.ui.inputimage.DatasetInputImage;
import sc.fiji.labkit.ui.segmentation.Segmenter;
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
