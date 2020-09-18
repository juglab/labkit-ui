
package net.imglib2.labkit.models;

import net.imglib2.Interval;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.utils.ParametricNotifier;
import net.imglib2.realtransform.AffineTransform3D;

/**
 * Represents a {@link Labeling} and provides additional information and
 * listeners.
 */
public interface LabelingModel {

	Holder<Label> selectedLabel();

	Holder<Labeling> labeling();

	ParametricNotifier<Interval> dataChangedNotifier();

	boolean isTimeSeries();

	AffineTransform3D labelTransformation();

	String defaultFileName();

	Holder<Boolean> labelingVisibility();

	TransformationModel transformationModel();
}
