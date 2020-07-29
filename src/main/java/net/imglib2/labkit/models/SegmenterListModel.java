
package net.imglib2.labkit.models;

import net.imagej.ImgPlus;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.segmentation.SegmentationPlugin;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.scijava.Context;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class SegmenterListModel {

	private final Context context;
	private final Holder<List<SegmentationItem>> segmenters = new DefaultHolder<>(new ArrayList<>());
	private final Holder<SegmentationItem> selectedSegmenter = new DefaultHolder<>(null);
	private final Holder<Boolean> segmentationVisibility = new DefaultHolder<>(true);
	private final Holder<List<Pair<ImgPlus<?>, Labeling>>> trainingData = new DefaultHolder<>(null);
	private final ImageLabelingModel imageLabelingModel;

	public SegmenterListModel(Context context, ImageLabelingModel imageLabelingModel) {
		this.context = context;
		this.imageLabelingModel = imageLabelingModel;
		this.trainingData.set(new SingletonTrainingData(imageLabelingModel));
	}

	public Holder<List<SegmentationItem>> segmenters() {
		return segmenters;
	}

	public Holder<SegmentationItem> selectedSegmenter() {
		return selectedSegmenter;
	}

	public SegmentationItem addSegmenter(SegmentationPlugin plugin) {
		// TODO: make this a controller
		SegmentationItem segmentationItem = new SegmentationItem(imageLabelingModel, plugin);
		segmenters.get().add(segmentationItem);
		segmenters.notifier().notifyListeners();
		return segmentationItem;
	}

	public void remove(SegmentationItem item) {
		segmenters.get().remove(item);
		if (!segmenters.get().contains(selectedSegmenter.get())) selectedSegmenter.set(null);
		segmenters.notifier().notifyListeners();
	}

	public Holder<Boolean> segmentationVisibility() {
		return segmentationVisibility;
	}

	public Context context() {
		return context;
	}

	public Holder<List<Pair<ImgPlus<?>, Labeling>>> trainingData() {
		return trainingData;
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
