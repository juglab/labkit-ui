
package net.imglib2.labkit.models;

import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.realtransform.AffineTransform3D;

public interface LabelingModel {

	Holder<Label> selectedLabel();

	Holder<Labeling> labeling();

	Notifier dataChangedNotifier();

	boolean isTimeSeries();

	AffineTransform3D labelTransformation();

	String defaultFileName();

	Holder<Boolean> labelingVisibility();

	TransformationModel transformationModel();
}
