
package sc.fiji.labkit.ui.models;

import net.imglib2.Interval;
import sc.fiji.labkit.ui.labeling.Label;
import sc.fiji.labkit.ui.labeling.Labeling;
import sc.fiji.labkit.ui.utils.ParametricNotifier;
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
