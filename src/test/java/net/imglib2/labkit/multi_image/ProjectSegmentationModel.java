
package net.imglib2.labkit.multi_image;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.labeling.LabelingSerializer;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.models.LabeledImage;
import net.imglib2.labkit.models.LabkitProjectModel;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmentationModel;
import net.imglib2.labkit.models.SegmenterListModel;
import net.imglib2.labkit.segmentation.PixelClassificationPlugin;
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

/**
 * ProjectSegmentationModel create a DefaultSegmentationModel that is linked to
 * a LabkitProjectModel. The DefaultSegmentationModel is updated, whenever the
 * {@link LabkitProjectModel#selectedImage()} changes.
 */
public class ProjectSegmentationModel implements SegmentationModel {

	private final Context context;

	private final LabkitProjectModel projectModel;

	private final ImageLabelingModel imageLabelingModel;

	private SegmenterListModel segmenterList;

	private final LabeledImage labeledImage;

	public ProjectSegmentationModel(LabkitProjectModel projectModel,
		SegmenterListModel segmenterListModel)
	{
		this.context = projectModel.context();
		this.labeledImage = projectModel.selectedImage().get();
		this.projectModel = projectModel;
		this.imageLabelingModel = openImageLabelingModel(labeledImage);
		this.segmenterList = segmenterListModel == null ? initSegmenterListModel(projectModel
			.segmenterFiles()) : segmenterListModel;
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

	public LabkitProjectModel projectModel() {
		return projectModel;
	}

	private void saveInDefaultLocation() {
//		LabeledImage image = projectModel.selectedImage().get();
//		if (image == lastSelectedImage)
//			return;
//		ImageLabelingModel imageLabelingModel = imageLabelingModel();
//		if (lastSelectedImage != null) {
//			try {
//				new LabelingSerializer(context).save(imageLabelingModel.labeling().get(),
//					lastSelectedImage.getLabelingFile());
//			}
//			catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		this.lastSelectedImage = image;
	}

	private SegmenterListModel initSegmenterListModel(List<String> segmenters) {
		SegmenterListModel segmenterListModel = new SegmenterListModel(context, imageLabelingModel);
		segmenterListModel.trainingData().set(new TrainingData());
		for (String filename : segmenters) {
			SegmentationItem segmentationItem = segmenterListModel.addSegmenter(PixelClassificationPlugin
				.create());
			segmentationItem.openModel(filename);
		}
		return segmenterListModel;
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
