
package net.imglib2.labkit.v2.image;

import net.imglib2.labkit.labeling.Label;
import net.imglib2.type.numeric.ARGBType;

import javax.swing.*;

public interface LabelsViewListener {

	void setActiveLabel(Label activeLabel);

	void addLabel();

	void removeAllLabels();

	void renameLabel(Label label, String newName);

	void setColor(Label label, ARGBType a);

	void focusLabel(Label label);

	void setLabelVisibility(Label label, boolean visible);

	JPopupMenu getPopupMenu(Label label);
}
