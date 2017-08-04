package net.imglib2.atlas;

import bdv.util.BdvHandle;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author Matthias Arzt
 */
public class FeatureLayer {

	private final SharedQueue queue;

	private final FeatureStack featureStack;

	private final RandomAccessibleContainer<FloatType> featureContainer;

	private final BdvHandle bdvHandle;

	public FeatureLayer(SharedQueue queue, FeatureStack featureStack, BdvHandle bdvHandle) {
		this.queue = queue;
		this.featureStack = featureStack;
		this.featureContainer = new RandomAccessibleContainer<>(tryWrapAsVolatile(featureStack.slices().get(0)));
		this.bdvHandle = bdvHandle;
	}

	public void selectFeature() {
		List<RandomAccessibleInterval<FloatType>> slices = featureStack.slices();
		List<String> names = featureStack.filter().attributeLabels();
		Object[] objects = IntStream.range(0, slices.size()).mapToObj(i -> new NamedValue<>(names.get(i), slices.get(i))).toArray();
		NamedValue<RandomAccessibleInterval<FloatType>> selected =
				(NamedValue<RandomAccessibleInterval<FloatType>>) JOptionPane.showInputDialog(null, "Index of Feature", "Select Feature",
						JOptionPane.PLAIN_MESSAGE, null, objects, 0);
		featureContainer.setSource(tryWrapAsVolatile(selected.get()));
		bdvHandle.getViewerPanel().requestRepaint();
	}

	public RandomAccessibleInterval<FloatType> getRandomAccessibleInterval() {
		return Views.interval(featureContainer, featureStack.interval());
	}

	private  <T> RandomAccessibleInterval<T> tryWrapAsVolatile(RandomAccessibleInterval<T> rai) {
		try
		{
			return AtlasUtils.uncheckedCast(VolatileViews.wrapAsVolatile(rai, queue));
		}
		catch ( final IllegalArgumentException e )
		{
			return rai;
		}
	}

	public AbstractNamedAction action() {
		return new AbstractNamedAction("Show Feature") {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				selectFeature();
			}
		};
	}

	private static class NamedValue<T> {
		private final String name;
		private final T value;

		NamedValue(String name, T value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public String toString() {
			return name;
		}

		public T get() {
			return value;
		}
	}
}
