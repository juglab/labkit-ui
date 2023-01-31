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

package sc.fiji.labkit.ui.actions;

import sc.fiji.labkit.ui.utils.HDF5Saver;
import io.scif.img.ImgSaver;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImgView;
import net.imglib2.img.display.imagej.ImageJFunctions;
import sc.fiji.labkit.ui.Extensible;
import sc.fiji.labkit.ui.models.ImageLabelingModel;
import sc.fiji.labkit.ui.models.SegmentationItem;
import sc.fiji.labkit.ui.models.SegmentationResultsModel;
import bdv.export.ProgressWriter;
import sc.fiji.labkit.ui.utils.ParallelUtils;
import sc.fiji.labkit.ui.utils.progress.SwingProgressWriter;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;

import javax.swing.*;

import java.util.function.Function;

/**
 * Implements the menu items that allow to save the segmentation and probability
 * map.
 *
 * @author Matthias Arzt
 */
public class SegmentationExportAction extends AbstractFileIoAction {

	private final ImageLabelingModel labelingModel;

	public SegmentationExportAction(Extensible extensible,
		ImageLabelingModel labelingModel)
	{
		super(extensible, AbstractFileIoAction.TIFF_FILTER,
			AbstractFileIoAction.HDF5_FILTER);
		this.labelingModel = labelingModel;
		addMenuItems("Segmentation Result",
			SegmentationResultsModel::segmentation,
			segmenter -> segmenter.classNames().size() - 1.0);
		addMenuItems("Probability Map",
			SegmentationResultsModel::prediction,
			ignore -> 1.0);
	}

	private <T extends NumericType<T> & NativeType<T>> void addMenuItems(String title,
		Function<SegmentationResultsModel, RandomAccessibleInterval<T>> getResultsImage,
		Function<SegmentationItem, Double> maxResultIntensity)
	{
		initSaveAction(SegmentationItem.SEGMENTER_MENU,
			"Save " + title + " as TIF / HDF5 ...", 200,
			(item, filename) -> saveImage(filename, getResultsImage.apply(item.results(labelingModel))),
			"");
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU,
			"Show " + title + " in ImageJ", 201,
			item -> onShowResultInImageJClicked(item, getResultsImage, maxResultIntensity),
			null, "");
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU,
			"Calculate entire " + title, 300,
			item -> onCalculateEntireResultClicked(getResultsImage, item),
			null, "");
	}

	private <T extends NumericType<T> & NativeType<T>> void onShowResultInImageJClicked(
		SegmentationItem item,
		Function<SegmentationResultsModel, RandomAccessibleInterval<T>> getResultsImage,
		Function<SegmentationItem, Double> maxResultIntensity)
	{
		SegmentationResultsModel results = item.results(labelingModel);
		RandomAccessibleInterval<T> result = getResultsImage.apply(results);
		double max = maxResultIntensity.apply(item);
		ParallelUtils.runInOtherThread(() -> populate(result));
		ParallelUtils.runInOtherThread(() -> ImageJFunctions.show(result).setDisplayRange(0, max));
	}

	private <T extends NumericType<T> & NativeType<T>> void onCalculateEntireResultClicked(
		Function<SegmentationResultsModel, RandomAccessibleInterval<T>> getResultsImage,
		SegmentationItem item)
	{
		final RandomAccessibleInterval<T> resultsImage = getResultsImage.apply(item.results(
			labelingModel));
		ParallelUtils.runInOtherThread(() -> populate(resultsImage));
	}

	private <T extends NumericType<T> & NativeType<T>> void populate(
		RandomAccessibleInterval<T> result)
	{
		final ProgressWriter progress = new SwingProgressWriter(null,
			"Segment Entire Image Volume");
		ParallelUtils.populateCachedImg(result, progress);
	}

	private <T extends Type<T>> void saveImage(String filename,
		RandomAccessibleInterval<T> image)
	{
		if (filename.endsWith(".h5") || filename.endsWith(".xml")) {
			final HDF5Saver saver = new HDF5Saver(image, filename);
			saver.setProgressWriter(new SwingProgressWriter(extensible.dialogParent(),
				"Save Image"));
			saver.writeAll();
		}
		else {
			try {
				ImgSaver saver = new ImgSaver(extensible.context());
				saver.saveImg(filename, ImgView.wrap(image, null));
			}
			catch (io.scif.img.ImgIOException e) {
				if (e.getCause() instanceof io.scif.FormatException)
					JOptionPane.showMessageDialog(null, "File format not supported:\n" + filename);
				else throw e;
			}
		}
	}
}
