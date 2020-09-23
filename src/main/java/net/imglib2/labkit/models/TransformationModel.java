
package net.imglib2.labkit.models;

import bdv.viewer.ViewerPanel;
import net.imglib2.Interval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.trainable_segmentation.RevampUtils;

import java.util.Arrays;
import java.util.Collections;

/**
 * Needs to be refactored, this is a strange way to enable the "reset view" and
 * "focus label" functionality.
 */
public class TransformationModel {

	private final boolean isTimeSeries;

	private ViewerPanel viewerPanel;

	public TransformationModel(boolean isTimeSeries) {
		this.isTimeSeries = isTimeSeries;
	}

	public void initialize(ViewerPanel viewerPanel) {
		this.viewerPanel = viewerPanel;
	}

	private int width() {
		return viewerPanel == null ? 100 : viewerPanel.getWidth();
	}

	private int height() {
		return viewerPanel == null ? 100 : viewerPanel.getHeight();
	}

	private void setTransformation(AffineTransform3D transformation) {
		if (viewerPanel != null) viewerPanel.setCurrentViewerTransform(
			transformation);
	}

	public void transformToShowInterval(Interval interval,
		AffineTransform3D transformation)
	{
		if (isTimeSeries) {
			int lastDim = interval.numDimensions() - 1;
			long meanTimePoint = (interval.min(lastDim) + interval.max(lastDim)) / 2;
			if (viewerPanel != null) viewerPanel.setTimepoint((int) meanTimePoint);
			interval = RevampUtils.removeLastDimension(interval);
		}
		final double[] screenSize = { width(), height() };
		AffineTransform3D concat = new AffineTransform3D();
		concat.set(getTransformation(interval, screenSize));
		concat.concatenate(transformation.inverse());
		setTransformation(concat);
	}

	private static AffineTransform3D getTransformation(Interval interval,
		double[] screenSize)
	{
		final double scale = 0.5 * getBiggestScaleFactor(screenSize, interval);
		final double[] translate = getTranslation(screenSize, interval, scale);
		final AffineTransform3D transform = new AffineTransform3D();
		transform.scale(scale);
		transform.translate(translate);
		return transform;
	}

	private static double[] getTranslation(final double[] screenSize,
		final Interval labelBox, final double labelScale)
	{
		final double[] translate = new double[3];
		for (int i = 0; i < Math.min(translate.length, labelBox
			.numDimensions()); i++)
		{
			translate[i] = -(labelBox.min(i) + labelBox.max(i)) * labelScale / 2;
			if (i < 2) {
				translate[i] += screenSize[i] / 2;
			}
		}
		return translate;
	}

	private static double getBiggestScaleFactor(final double[] screenSize,
		final Interval labelBox)
	{
		final Double[] scales = new Double[2];
		final double minLength = 20.0;
		for (int i = 0; i < 2; i++)
			scales[i] = screenSize[i] / Math.max(labelBox.max(i) - labelBox.min(i),
				minLength);
		return Collections.min(Arrays.asList(scales));
	}
}
