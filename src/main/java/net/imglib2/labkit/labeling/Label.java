
package net.imglib2.labkit.labeling;

import net.imglib2.labkit.menu.MenuKey;
import net.imglib2.type.numeric.ARGBType;

public class Label {

	public final static MenuKey<Label> LABEL_MENU = new MenuKey<>(Label.class);

	private String name;

	private boolean active;

	private final ARGBType color;

	public Label(String name, ARGBType color) {
		this.name = name;
		this.color = new ARGBType();
		this.color.set(color);
		this.active = true;
	}

	public String name() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ARGBType color() {
		return color;
	}

	public void setColor(ARGBType color) {
		this.color.set(color);
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
