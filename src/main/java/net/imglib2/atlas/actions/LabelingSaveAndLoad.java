package net.imglib2.atlas.actions;

import net.imglib2.atlas.LabelingComponent;
import net.imglib2.atlas.MainFrame;
import net.imglib2.atlas.labeling.Labeling;
import net.imglib2.atlas.labeling.LabelingSerializer;

import java.io.IOException;

/**
 * @author Matthias Arzt
 */
public class LabelingSaveAndLoad extends AbstractSaveAndLoadAction {

	private final LabelingComponent labelingComponent;

	public LabelingSaveAndLoad(MainFrame.Extensible extensible, LabelingComponent labelingComponent) {
		super(extensible);
		this.labelingComponent = labelingComponent;
		initAction("Save Labeling", this::save, "");
		initAction("Load Labeling", this::load, "");
	}

	private void save(String filename) throws IOException {
		LabelingSerializer.save(labelingComponent.getLabeling(), filename);
	}

	private void load(String filename) throws IOException {
		Labeling labeling = LabelingSerializer.load(filename);
		labelingComponent.setLabeling(labeling);
	}
}
