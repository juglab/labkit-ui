
package net.imglib2.labkit.multi_image;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imagej.patcher.LegacyInjector;
import net.imglib2.labkit.LabkitFrame;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.labeling.LabelingSerializer;
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.models.MultiImageSegmentationModel;
import net.imglib2.labkit.utils.CheckedExceptionUtils;
import net.imglib2.trainable_segmentation.utils.SingletonContext;
import net.imglib2.type.numeric.NumericType;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiImageDemo {

	static {
		LegacyInjector.preinit();
	}

	private final static Context context = SingletonContext.getInstance();

	private static List<ImageItem> files = Stream.of(
		"/home/arzt/tmp/labkit-project/phase1.tif",
		"/home/arzt/tmp/labkit-project/phase2.tif",
		"/home/arzt/tmp/labkit-project/phase3.tif",
		"/home/arzt/tmp/labkit-project/phase4.tif")
		.map(ImageItem::new)
		.collect(Collectors.toList());

	public static void main(String... args) {
		List<ImageLabelingModel> imageLabelingModels = files.stream()
			.map(ii -> openImageLabelingModel(ii)).collect(Collectors.toList());
		MultiImageSegmentationModel multiImageSegmentationModel = new MultiImageSegmentationModel(
			SingletonContext.getInstance(), imageLabelingModels);
		JList<ImageLabelingModel> list = initList(imageLabelingModels);
		JButton button = initButton(list, multiImageSegmentationModel);
		showFrame(list, button);
	}

	private static void showFrame(JList<?> list, JButton button) {
		JFrame frame = new JFrame("Labkit Project");
		frame.setLayout(new MigLayout("", "[grow]", "[grow][]"));
		frame.add(new JScrollPane(list), "grow, wrap");
		frame.add(button);
		frame.pack();
		frame.setVisible(true);
	}

	private static JList<ImageLabelingModel> initList(List<ImageLabelingModel> imageLabelingModels) {
		return new JList<>(imageLabelingModels.toArray(new ImageLabelingModel[0]));
	}

	private static JButton initButton(JList<ImageLabelingModel> comp,
		MultiImageSegmentationModel multiImageSegmentationModel)
	{
		JButton button = new JButton("edit");
		button.addActionListener(l -> {
			ImageLabelingModel selectedValue = comp.getSelectedValue();
			if (selectedValue != null)
				selectionChanged(selectedValue, multiImageSegmentationModel);
		});
		return button;
	}

	private static void selectionChanged(ImageLabelingModel imageLabelingModel,
		MultiImageSegmentationModel multiImageSegmentationModel)
	{
		final DefaultSegmentationModel model = new DefaultSegmentationModel(context,
			multiImageSegmentationModel.imageLabelingModels(),
			imageLabelingModel,
			multiImageSegmentationModel.segmenterListModel());
		LabkitFrame frame = LabkitFrame.show(model, model.imageLabelingModel().imageForSegmentation()
			.get()
			.getName());
		frame.onCloseListeners().add(() -> {
			saveImageLabelingModel(model);
		});
	}

	private static ImageLabelingModel openImageLabelingModel(ImageItem item) {
		DatasetIOService datasetIOService = context.service(DatasetIOService.class);
		Dataset dataset = CheckedExceptionUtils.run(() -> datasetIOService.open(item.getImageFile()));
		DatasetInputImage inputImage = new DatasetInputImage(dataset);
		inputImage.setDefaultLabelingFilename(item.getLabelingFile());
		ImageLabelingModel imageLabelingModel = new ImageLabelingModel(inputImage);
		Labeling labeling = openOrEmptyLabeling(inputImage);
		imageLabelingModel.labeling().set(labeling);
		return imageLabelingModel;
	}

	private static void saveImageLabelingModel(DefaultSegmentationModel model) {
		try {
			new LabelingSerializer(context).save(model.imageLabelingModel().labeling().get(),
				model.imageLabelingModel().defaultFileName());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Labeling openOrEmptyLabeling(DatasetInputImage inputImage) {
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
}
