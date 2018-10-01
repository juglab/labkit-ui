
package net.imglib2.labkit.models;

import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.labkit.color.ColorMapProvider;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.realtransform.AffineTransform3D;

import java.util.Set;

public interface LabelingModel {

	Holder<Label> selectedLabel();

	ColorMapProvider colorMapProvider();

	Holder<Labeling> labeling();

	Notifier<Runnable> dataChangedNotifier();

	Holder<Set<Label>> activeLabels();

	boolean isTimeSeries();

	AffineTransform3D labelTransformation();

	String defaultFileName();
}
