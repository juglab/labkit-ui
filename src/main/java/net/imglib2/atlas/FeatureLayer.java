package net.imglib2.atlas;

import bdv.util.BdvStackSource;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.type.volatiles.VolatileFloatType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.view.Views;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Matthias Arzt
 */
public class FeatureLayer {

	private final FeatureStack featureStack;

	private final RandomAccessibleContainer<VolatileFloatType> featureContainer;

	private final MainFrame.Extensible extensible;

	private Choice selected;

	public FeatureLayer(MainFrame.Extensible extensible, FeatureStack featureStack) {
		this.extensible = extensible;
		this.featureStack = featureStack;
		this.featureContainer = new RandomAccessibleContainer<>(null);
		setSelected(0);
		addLayer();
		this.extensible.addAction("Show Feature", "showFeature", this::selectFeature, "");
		new MouseWheelChannelSelector(extensible, this);
		featureStack.listeners().add(() -> setSelected(selected.index));
	}

	private void addLayer() {
		final BdvStackSource source = extensible.addLayer(Views.interval(featureContainer, featureStack.interval()), "feature");
		source.setDisplayRange( 0, 255 );
		source.setActive( false );
	}

	private Choice emptyChoice() {
		RandomAccessible<FloatType> empty = ConstantUtils.constantRandomAccessible(new FloatType(0.0f), featureStack.interval().numDimensions());
		return new Choice("no feature", 0, Views.interval(empty, featureStack.interval()));
	}

	public void selectFeature() {
		Choice selected = (Choice) JOptionPane.showInputDialog(null, "Index of Feature", "Select Feature",
						JOptionPane.PLAIN_MESSAGE, null, getChoices().toArray(), 0);
		setSource(selected);
	}

	private List<Choice> getChoices() {
		List<RandomAccessibleInterval<FloatType>> slices = featureStack.slices();
		List<String> names = featureStack.filter().attributeLabels();
		return IntStream.range(0, slices.size()).mapToObj(i -> new Choice(names.get(i), i, slices.get(i)))
				.collect(Collectors.toList());
	}

	private void setSource(Choice selected) {
		this.selected = selected;
		featureContainer.setSource(tryWrapAsVolatile(selected.value));
		extensible.repaint();
	}

	private  <T, V extends Volatile<T>> RandomAccessibleInterval<V> tryWrapAsVolatile(RandomAccessibleInterval<T> rai) {
		return AtlasUtils.uncheckedCast(extensible.wrapAsVolatile(rai));
	}

	private void setSelected(int index) {
		List<Choice> choices = getChoices();
		if(choices.isEmpty())
			setSource(emptyChoice());
		else
			setSource(choices.get(Math.max(0, Math.min(choices.size() - 1, index))));
	}

	public void next() {
		setSelected(selected.index + 1);
	}

	public void previous() {
		setSelected(selected.index - 1);
	}

	public String title() {
		return selected.toString();
	}

	private static class Choice {
		private final String name;
		private final int index;
		private final RandomAccessibleInterval<FloatType> value;

		Choice(String name, int index, RandomAccessibleInterval<FloatType> value) {
			this.name = name;
			this.index = index;
			this.value = value;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
