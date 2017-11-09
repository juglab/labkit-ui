package net.imglib2.atlas.actions;

import net.imglib2.atlas.Extensible;
import net.imglib2.atlas.InputImage;
import net.imglib2.atlas.LabelingComponent;
import net.imglib2.atlas.MainFrame;
import net.imglib2.atlas.labeling.Labeling;
import net.imglib2.atlas.labeling.LabelingSerializer;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;

/**
 * @author Matthias Arzt
 */
public class LabelingIoAction extends AbstractFileIoAcion {

	private final LabelingComponent labelingComponent;
	private final LabelingSerializer serializer;

	public LabelingIoAction(Extensible extensible, LabelingComponent labelingComponent, InputImage inputImage) {
		super(extensible, new FileNameExtensionFilter("Labeling (*.labeling)", "labeling"));
		this.labelingComponent = labelingComponent;
		serializer = new LabelingSerializer(extensible.context());
		initSaveAction("Save Labeling ...", "saveLabeling", new Action() {
			@Override
			public String suggestedFile() {
				return inputImage.getFilename() + ".labeling";
			}

			@Override
			public void run(String filename) throws Exception {
				serializer.save(labelingComponent.getLabeling(), filename);
			}
		}, "ctrl S");
		initOpenAction("Open Labeling ...", "openLabeling", this::open, "ctrl O");
	}

	private void open(String filename) throws IOException {
		Labeling labeling = serializer.open(filename);
		labelingComponent.setLabeling(labeling);
	}
}
