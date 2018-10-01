
package net.imglib2.labkit.control.brush;

import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.models.LabelingModel;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class ChangeLabel extends AbstractNamedAction {

	private final LabelingModel model;

	public ChangeLabel(LabelingModel model) {
		super("Next Label");
		this.model = model;
		super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("N"));
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		List<Label> labels = model.labeling().get().getLabels();
		Label nextLabel = next(labels, model.selectedLabel().get());
		if (nextLabel != null) model.selectedLabel().set(nextLabel);
	}

	private Label next(List<Label> labels, Label currentLabel) {
		if (labels.isEmpty()) return null;
		int index = labels.indexOf(currentLabel) + 1;
		if (index >= labels.size()) index = 0;
		return labels.get(index);
	}
}
