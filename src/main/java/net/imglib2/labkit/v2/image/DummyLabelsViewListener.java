
package net.imglib2.labkit.v2.image;

import net.imglib2.labkit.labeling.Label;
import net.imglib2.type.numeric.ARGBType;

import javax.swing.*;

public class DummyLabelsViewListener implements LabelsViewListener {

	@Override
	public void setActiveLabel(Label activeLabel) {

	}

	@Override
	public void addLabel() {

	}

	@Override
	public void removeAllLabels() {

	}

	@Override
	public void renameLabel(Label label, String newName) {

	}

	@Override
	public void setColor(Label label, ARGBType a) {

	}

	@Override
	public void focusLabel(Label label) {

	}

	@Override
	public void setLabelVisibility(Label label, boolean visible) {

	}

	@Override
	public JPopupMenu getPopupMenu(Label label) {
		return new JPopupMenu();
	}
}
