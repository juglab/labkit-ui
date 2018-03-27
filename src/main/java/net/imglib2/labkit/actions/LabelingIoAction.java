package net.imglib2.labkit.actions;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.labeling.LabelingSerializer;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.IntegerType;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;

/**
 * @author Matthias Arzt
 */
public class LabelingIoAction extends AbstractFileIoAcion {

	private final Holder<Labeling> labeling;
	private final LabelingSerializer serializer;

	public LabelingIoAction(Extensible extensible, Holder<Labeling> labeling, InputImage inputImage) {
		super(extensible, new FileNameExtensionFilter("Labeling (*.labeling)", "labeling"));
		this.labeling = labeling;
		serializer = new LabelingSerializer(extensible.context());
		initSaveAction("Save Labeling ...", "saveLabeling", new Action() {
			@Override
			public String suggestedFile() {
				return inputImage.getLabelingName() + ".labeling";
			}

			@Override
			public void run(String filename) throws Exception {
				serializer.save(labeling.get(), filename);
			}
		}, "ctrl S");
		initOpenAction("Open Labeling ...", "openLabeling", this::open, "ctrl O");
		extensible.addAction("Show Labeling in ImageJ", "showLabeling", () -> {
			RandomAccessibleInterval<? extends IntegerType<?>> img = labeling.get().getIndexImg();
			ImageJFunctions.show(LabkitUtils.uncheckedCast(img), "Labeling");
		}, "");
	}

	private void open(String filename) throws IOException {
		Labeling labeling = serializer.open(filename);
		this.labeling.set(labeling);
	}
}
