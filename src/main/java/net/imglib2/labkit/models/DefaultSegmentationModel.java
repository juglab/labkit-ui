
package net.imglib2.labkit.models;

import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.utils.DimensionUtils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.scijava.Context;

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

	public <T extends IntegerType<T> & NativeType<T>>
		List<RandomAccessibleInterval<T>> getSegmentations(T type)
	{
		ImgPlus<?> image = imageLabelingModel().imageForSegmentation().get();
		Stream<Segmenter> trainedSegmenters = getTrainedSegmenters();
		return trainedSegmenters.map(segmenter -> {
			RandomAccessibleInterval<T> labels = new CellImgFactory<>(type).create(
				image);
			segmenter.segment(image, labels);
			return labels;
		}).collect(Collectors.toList());
	}

	public List<RandomAccessibleInterval<FloatType>> getPredictions() {
		ImgPlus<?> image = imageLabelingModel().imageForSegmentation().get();
		Stream<Segmenter> trainedSegmenters = getTrainedSegmenters();
		return trainedSegmenters.map(segmenter -> {
			int numberOfClasses = segmenter.classNames().size();
			RandomAccessibleInterval<FloatType> prediction = new CellImgFactory<>(
				new FloatType()).create(DimensionUtils.appendDimensionToInterval(image,
					0, numberOfClasses - 1));
			segmenter.predict(image, prediction);
			return prediction;
		}).collect(Collectors.toList());
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
