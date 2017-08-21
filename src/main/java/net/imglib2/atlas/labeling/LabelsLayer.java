package net.imglib2.atlas.labeling;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.atlas.Holder;
import net.imglib2.atlas.RandomAccessibleContainer;
import net.imglib2.atlas.color.IntegerARGBConverters;
import net.imglib2.atlas.color.IntegerColorProvider;
import net.imglib2.converter.Converters;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.view.Views;

/**
 * @author Matthias Arzt
 */
public class LabelsLayer {

	private final IntegerColorProvider colorProvider;

	private final RandomAccessibleContainer<ARGBType> container;

	private RandomAccessibleInterval<ARGBType> view;

	public LabelsLayer(Holder<Labeling> labeling, IntegerColorProvider colorProvider) {
		this.colorProvider = colorProvider;
		container = new RandomAccessibleContainer<>(colorView(labeling.get()));
		view = Views.interval(container, labeling.get());
		labeling.notifier().add(this::updateLabeling);
	}

	private void updateLabeling(Labeling labeling) {
		container.setSource(colorView(labeling));
	}

	private RandomAccessibleInterval<ARGBType> colorView(Labeling labeling) {
		return Converters.convert( labeling.intView(), new IntegerARGBConverters.ARGB<>( colorProvider ), new ARGBType() );
	}

	public RandomAccessibleInterval<ARGBType> view() {
		return view;
	}
}
