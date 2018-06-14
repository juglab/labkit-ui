
package net.imglib2.labkit.labeling;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.bdv.BdvLayer;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.labkit.models.LabelingModel;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.labkit.utils.RandomAccessibleContainer;
import net.imglib2.labkit.color.ColorMap;
import net.imglib2.converter.Converters;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.view.Views;

import java.util.List;
import java.util.Set;

/**
 * @author Matthias Arzt
 */
public class LabelsLayer implements BdvLayer {

	private final RandomAccessibleContainer<ARGBType> container;

	private final LabelingModel model;

	private final RandomAccessibleInterval<ARGBType> view;

	private final Notifier<Runnable> listeners = new Notifier<>();

	public LabelsLayer(LabelingModel model) {
		this.model = model;
		RandomAccessibleInterval<ARGBType> view = colorView();
		container = new RandomAccessibleContainer<>(view);
		this.view = Views.interval(container, view);
		model.labeling().notifier().add(this::updateLabeling);
		model.dataChangedNotifier().add(() -> listeners.forEach(Runnable::run));
	}

	private void updateLabeling(Labeling labeling) {
		container.setSource(colorView());
		listeners.forEach(Runnable::run);
	}

	private RandomAccessibleInterval<ARGBType> colorView() {
		Labeling labeling = model.labeling().get();
		ColorMap colorMap = model.colorMapProvider().colorMap();
		List< Set< String > > labelSets = labeling.getLabelSets();
		TIntObjectMap<ARGBType> colors = new TIntObjectHashMap<>();

		return Converters.convert(labeling.getIndexImg(), (in, out) -> {
			int i = in.getInteger();
			ARGBType c = colors.get(i);
			if (c == null) {
				c = getColor( colorMap, labelSets.get(i) );
				colors.put(i, c);
			}
			out.set(c);
		}, new ARGBType());
	}

	private ARGBType getColor( ColorMap colorMap, Set< String > set )
	{
		ARGBType result = new ARGBType();
		double factor = 1.0 / set.size();
		set.forEach( label -> result.add(downScale(factor, colorMap.getColor( label ))) );
		return result;
	}

	private ARGBType downScale( double factor, ARGBType color )
	{
		ARGBType result = color.copy();
		result.mul( factor );
		return result;
	}

	@Override
	public BdvShowable image() {
		return BdvShowable.wrap(view, model.labelTransformation());
	}

	@Override
	public Notifier<Runnable> listeners() {
		return listeners;
	}

	@Override
	public String title() {
		return "Labeling";
	}
}
