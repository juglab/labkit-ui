
package net.imglib2.labkit.multi_image;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.labeling.LabelingSerializer;
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.models.LabeledImage;
import net.imglib2.labkit.models.LabkitProjectModel;
import net.imglib2.labkit.models.SegmentationModel;
import net.imglib2.labkit.models.SegmenterListModel;
import net.imglib2.labkit.utils.CheckedExceptionUtils;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;
import java.util.AbstractList;
import java.util.Arrays;

/**
 * ProjectSegmentationModel create a DefaultSegmentationModel that is linked to
 * a LabkitProjectModel. The DefaultSegmentationModel is updated, whenever the
 * {@link LabkitProjectModel#selectedImage()} changes.
 */
public class ProjectSegmentationModel {

	private final Context context;

	private final LabkitProjectModel projectModel;

	private final SegmentationModel segmentationModel;

	private LabeledImage lastSelectedImage;

	public static SegmentationModel init(LabkitProjectModel labkitProjectModel) {
		return new ProjectSegmentationModel(labkitProjectModel).getSegmentationModel();
	}

	public ProjectSegmentationModel(LabkitProjectModel projectModel) {
		this.context = projectModel.context();
		this.projectModel = projectModel;
		this.segmentationModel = initSegmentationModel();
		this.projectModel.selectedImage().notifier().add(this::onSelectedImageChanged);
	}

	public LabkitProjectModel getProjectModel() {
		return projectModel;
	}

	public SegmentationModel getSegmentationModel() {
		return segmentationModel;
	}

	private void onSelectedImageChanged() {
		LabeledImage image = projectModel.selectedImage().get();
		if (image == lastSelectedImage)
			return;
		ImageLabelingModel imageLabelingModel = segmentationModel.imageLabelingModel();
		if (lastSelectedImage != null) {
			try {
				new LabelingSerializer(context).save(imageLabelingModel.labeling().get(),
					lastSelectedImage.getLabelingFile());
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		DatasetInputImage ii = openInputImage(image);
		imageLabelingModel.imageForSegmentation().set(ii.imageForSegmentation());
		imageLabelingModel.showable().set(ii.showable());
		imageLabelingModel.labeling().set(openOrEmptyLabeling(ii));
		imageLabelingModel.transformationModel().transformToShowInterval(imageLabelingModel
			.imageForSegmentation().get(),
			imageLabelingModel.labelTransformation());
		this.lastSelectedImage = image;
	}

	private SegmentationModel initSegmentationModel() {
		ImageLabelingModel imageLabelingModel = openImageLabelingModel(projectModel.selectedImage()
			.get());
		SegmenterListModel segmenterListModel = new SegmenterListModel(context, imageLabelingModel);
		SegmentationModel model = new DefaultSegmentationModel(context, imageLabelingModel,
			segmenterListModel);
		model.segmenterList().trainingData().set(new TrainingData());
		return model;
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

	private class TrainingData extends AbstractList<Pair<ImgPlus<?>, Labeling>> {

		@Override
		public Pair<ImgPlus<?>, Labeling> get(int index) {
			LabeledImage imageItem = projectModel.labeledImages().get(index);
			if (imageItem == projectModel.selectedImage()) {
				ImageLabelingModel imageLabelingModel = segmentationModel.imageLabelingModel();
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
			return projectModel.labeledImages().size();
		}
	}
}
