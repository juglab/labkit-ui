package net.imglib2.labkit.models;

import net.imglib2.labkit.utils.Notifier;
import net.imglib2.labkit.color.ColorMapProvider;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.realtransform.AffineTransform3D;

public interface LabelingModel {

	Holder<String> selectedLabel();

	ColorMapProvider colorMapProvider();

	Holder<Labeling> labeling();

	Notifier<Runnable> dataChangedNotifier();

	boolean isTimeSeries();

	AffineTransform3D labelTransformation();
}
