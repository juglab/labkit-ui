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

import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import sc.fiji.labkit.ui.inputimage.InputImage;
import sc.fiji.labkit.ui.labeling.Labeling;
import sc.fiji.labkit.ui.segmentation.SegmentationTool;
import sc.fiji.labkit.ui.segmentation.Segmenter;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.scijava.Context;
import sc.fiji.labkit.ui.utils.progress.DummyProgressWriter;

import java.util.AbstractList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link SegmentationModel} that works with one fixed image.
 */
public class DefaultSegmentationModel implements SegmentationModel {

	private final Context context;
	private final ImageLabelingModel imageLabelingModel;
	private final SegmenterListModel segmenterList;
	private final ExtensionPoints extensionPoints = new ExtensionPoints();

	public DefaultSegmentationModel(Context context, InputImage inputImage) {
		this.context = context;
		this.imageLabelingModel = new ImageLabelingModel(inputImage);
		this.segmenterList = new SegmenterListModel(context, extensionPoints);
		this.segmenterList().trainingData().set(new SingletonTrainingData(imageLabelingModel));
	}

	@Override
	public Context context() {
		return context;
	}

	@Override
	public ImageLabelingModel imageLabelingModel() {
		return imageLabelingModel;
	}

	@Override
	public SegmenterListModel segmenterList() {
		return segmenterList;
	}

	public ExtensionPoints extensionPoints() {
		return extensionPoints;
	}

	@Deprecated
	public <T extends IntegerType<T> & NativeType<T>>
		List<RandomAccessibleInterval<T>> getSegmentations(T type)
	{
		ImgPlus<?> image = imageLabelingModel().imageForSegmentation().get();
		Stream<Segmenter> trainedSegmenters = getTrainedSegmenters();
		return trainedSegmenters
			.map(segmenter -> {
				SegmentationTool segmentationTool = new SegmentationTool(segmenter);
				segmentationTool.setProgressWriter(new DummyProgressWriter());
				return segmentationTool.segment(image, type);
			})
			.collect(Collectors.toList());
	}

	@Deprecated
	public List<RandomAccessibleInterval<FloatType>> getPredictions() {
		ImgPlus<?> image = imageLabelingModel().imageForSegmentation().get();
		Stream<Segmenter> trainedSegmenters = getTrainedSegmenters();
		return trainedSegmenters
			.map(segmenter -> {
				SegmentationTool segmentationTool = new SegmentationTool(segmenter);
				segmentationTool.setProgressWriter(new DummyProgressWriter());
				return segmentationTool.probabilityMap(image);
			})
			.collect(Collectors.toList());
	}

	public boolean isTrained() {
		return getTrainedSegmenters().findAny().isPresent();
	}

	private Stream<Segmenter> getTrainedSegmenters() {
		return segmenterList.segmenters().get().stream().filter(Segmenter::isTrained).map(x -> x);
	}

	private class SingletonTrainingData extends AbstractList<Pair<ImgPlus<?>, Labeling>> {

		private final ImageLabelingModel imageLabelingModel;

		public SingletonTrainingData(ImageLabelingModel imageLabelingModel) {
			this.imageLabelingModel = imageLabelingModel;
		}

		@Override
		public Pair<ImgPlus<?>, Labeling> get(int index) {
			ImgPlus<?> image = imageLabelingModel.imageForSegmentation().get();
			Labeling labeling = imageLabelingModel.labeling().get();
			return new ValuePair<>(image, labeling);
		}

		@Override
		public int size() {
			return 1;
		}
	}
}
