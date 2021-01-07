
package net.imglib2.labkit.v2.image;

public interface ToolBarViewListener {

	void setOverrideFlag(boolean overrideFlag);

	void setBrushRadius(int value);

	void setMode(ToolBarView.Mode model);
}
