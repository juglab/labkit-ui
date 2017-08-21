package net.imglib2.atlas.actions;

import com.google.gson.GsonBuilder;
import net.imglib2.atlas.LabelingComponent;
import net.imglib2.atlas.MainFrame;
import net.imglib2.atlas.labeling.Labeling;
import net.imglib2.atlas.labeling.LabelingSerializer;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author Matthias Arzt
 */
public class LabelingSaveAndLoad {

	private final MainFrame.Extensible extensible;

	private final LabelingComponent labelingComponent;

	public LabelingSaveAndLoad(MainFrame.Extensible extensible, LabelingComponent labelingComponent) {
		this.extensible = extensible;
		this.labelingComponent = labelingComponent;
		extensible.addAction(new AbstractNamedAction("save labeling") {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				saveLabeling();
			}
		}, "");
		extensible.addAction(new AbstractNamedAction("load labeling") {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				loadLabeling();
			}
		}, "");
	}

	private void loadLabeling() {
		fileChooserAndThen(filename -> {
			try {
				Labeling labeling = LabelingSerializer.load(filename);
				labelingComponent.setLabeling(labeling);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private void saveLabeling() {
		fileChooserAndThen(filename -> {
			try {
				LabelingSerializer.save(labelingComponent.getLabeling(), filename);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private void fileChooserAndThen(Consumer<String> action) {
		final JFileChooser fileChooser = new JFileChooser();
		final int returnVal = fileChooser.showOpenDialog(extensible.dialogParent());
		if ( returnVal == JFileChooser.APPROVE_OPTION )
				action.accept(fileChooser.getSelectedFile().getAbsolutePath());
	}
}
