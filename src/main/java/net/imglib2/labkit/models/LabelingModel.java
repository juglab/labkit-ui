
package net.imglib2.labkit.models;

import net.imglib2.Interval;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.utils.properties.Property;
import net.imglib2.labkit.utils.ParametricNotifier;
import net.imglib2.realtransform.AffineTransform3D;

/**
 * Represents a {@link Labeling} and provides additional information and
 * listeners.
 */
public interface LabelingModel {

	Property<Label> selectedLabel();

	Property<Labeling> labeling();

	ParametricNotifier<Interval> dataChangedNotifier();

	boolean isTimeSeries();

	AffineTransform3D labelTransformation();

	String defaultFileName();

	Property<Boolean> labelingVisibility();

	TransformationModel transformationModel();
}
