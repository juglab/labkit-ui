
package net.imglib2.labkit.models;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.labeling.LabelingSerializer;
import net.imglib2.labkit.utils.CheckedExceptionUtils;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

public class LabkitProjectModel {

	private final Context context;

	private final DefaultSegmentationModel model;

	private LabeledImage selectedImageItem;

	private List<LabeledImage> labeledImages;

	public LabkitProjectModel(Context context,
		List<LabeledImage> labeledImages)
	{
		this.context = context;
		this.selectedImageItem = labeledImages.get(0);
		this.labeledImages = labeledImages;
		ImageLabelingModel imageLabelingModel = openImageLabelingModel(selectedImageItem);
		SegmenterListModel segmenterListModel = new SegmenterListModel(context, imageLabelingModel);
		this.model = new DefaultSegmentationModel(context, imageLabelingModel, segmenterListModel);
		this.model.segmenterList().trainingData().set(new TrainingData());
	}

	private ImageLabelingModel openImageLabelingModel(LabeledImage item) {
		DatasetInputImage inputImage = openInputImage(item);
		Labeling labeling = openOrEmptyLabeling(inputImage);
		ImageLabelingModel imageLabelingModel = new ImageLabelingModel(inputImage);
		imageLabelingModel.labeling().set(labeling);
		return imageLabelingModel;
	}

	private DatasetInputImage openInputImage(LabeledImage item) {
		DatasetIOService datasetIOService = context.service(DatasetIOService.class);
		Dataset dataset = CheckedExceptionUtils.run(() -> datasetIOService.open(item.getImageFile()));
		DatasetInputImage inputImage = new DatasetInputImage(dataset);
		inputImage.setDefaultLabelingFilename(item.getLabelingFile());
		return inputImage;
	}

	private Labeling openOrEmptyLabeling(DatasetInputImage inputImage) {
		String defaultLabelingFilename = inputImage.getDefaultLabelingFilename();
		if (new File(defaultLabelingFilename).exists()) {
			try {
				return new LabelingSerializer(context).open(defaultLabelingFilename);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		ImgPlus<? extends NumericType<?>> interval = inputImage.imageForSegmentation();
		return Labeling.createEmpty(Arrays.asList("background", "foreground"), interval);
	}

	public void selectLabeledImage(LabeledImage image) {
		ImageLabelingModel imageLabelingModel = model.imageLabelingModel();
		try {
			new LabelingSerializer(context).save(imageLabelingModel.labeling().get(),
				selectedImageItem.getLabelingFile());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		selectedImageItem = image;
		DatasetInputImage ii = openInputImage(image);
		imageLabelingModel.imageForSegmentation().set(ii.imageForSegmentation());
		imageLabelingModel.showable().set(ii.showable());
		imageLabelingModel.labeling().set(openOrEmptyLabeling(ii));
		imageLabelingModel.transformationModel().transformToShowInterval(imageLabelingModel
			.imageForSegmentation().get(),
			imageLabelingModel.labelTransformation());
	}

	public DefaultSegmentationModel segmentationModel() {
		return model;
	}

	public List<LabeledImage> labeledImages() {
		return labeledImages;
	}

	public Context context() {
		return context;
	}

	private class TrainingData extends AbstractList<Pair<ImgPlus<?>, Labeling>> {

		@Override
		public Pair<ImgPlus<?>, Labeling> get(int index) {
			LabeledImage imageItem = labeledImages.get(index);
			if (imageItem == selectedImageItem) {
				ImageLabelingModel imageLabelingModel = model.imageLabelingModel();
				ImgPlus<?> image = imageLabelingModel.imageForSegmentation().get();
				Labeling labeling = imageLabelingModel.labeling().get();
				return new ValuePair<>(image, labeling);
			}
			DatasetInputImage ii = openInputImage(imageItem);
			ImgPlus<?> image = ii.imageForSegmentation();
			Labeling labeling = openOrEmptyLabeling(ii);
			return new ValuePair<>(image, labeling);
		}

		@Override
		public int size() {
			return labeledImages.size();
		}
	}
}
