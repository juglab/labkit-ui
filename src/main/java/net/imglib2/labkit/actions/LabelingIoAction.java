
package net.imglib2.labkit.actions;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.DefaultExtensible;
import net.imglib2.labkit.models.LabelingModel;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.labeling.LabelingSerializer;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.IntegerType;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;

/**
 * @author Matthias Arzt
 */
public class LabelingIoAction extends AbstractFileIoAction {

	private final LabelingModel labelingModel;
	private final LabelingSerializer serializer;

	public LabelingIoAction(DefaultExtensible extensible,
		LabelingModel labelingModel)
	{
		super(extensible, new FileNameExtensionFilter("Labeling (*.labeling)",
			"labeling"));
		this.labelingModel = labelingModel;
		serializer = new LabelingSerializer(extensible.context());
		initSaveAction("Save Labeling ...", "saveLabeling", new Action() {

			@Override
			public String suggestedFile() {
				return LabelingIoAction.this.labelingModel.defaultFileName();
			}

			@Override
			public void run(String filename) throws Exception {
				serializer.save(labelingModel.labeling().get(), filename);
			}
		}, "ctrl S");
		initOpenAction("Open Labeling ...", "openLabeling", this::open, "ctrl O");
		extensible.addAction("Show Labeling in ImageJ", "showLabeling", () -> {
			RandomAccessibleInterval<? extends IntegerType<?>> img = labelingModel
				.labeling().get().getIndexImg();
			ImageJFunctions.show(LabkitUtils.uncheckedCast(img), "Labeling");
		}, "");
	}

	private void open(String filename) throws IOException {
		Labeling labeling = serializer.open(filename);
		labelingModel.labeling().set(labeling);
	}
}
