
package net.imglib2.labkit.menu;

import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.models.SegmentationItem;

/**
 * User together with {@link MenuFactory}, to define sub-menus or popup menus.
 * <p>
 * The popup menus in Labkit are associated with a class. There is a popup menu
 * for the {@link SegmentationItem} class and for the {@link Label} class. And
 * there are multiple menus associated with no class, where {@link Void} is
 * used.
 */
public class MenuKey<T> {

	private final Class<T> inputParameterClass;

	public MenuKey(Class<T> inputParameterClass) {
		this.inputParameterClass = inputParameterClass;
	}

	public Class<T> inputParameterClass() {
		return inputParameterClass;
	}
}
