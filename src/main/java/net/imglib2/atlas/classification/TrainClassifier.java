package net.imglib2.atlas.classification;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.imglib2.atlas.MainFrame;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import gnu.trove.map.hash.TLongIntHashMap;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.IntervalIndexer;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import net.imglib2.view.composite.CompositeView;
import net.imglib2.view.composite.RealComposite;

public class TrainClassifier< F extends RealType< F > > extends AbstractNamedAction
{

	private TLongIntHashMap groundTruth;

	private Iterator<Pair<Composite<? extends RealType<?>>,? extends IntegerType<?>>> toSamples(RandomAccessibleInterval<F> features, long[] locations, int[] labels) {
		final CompositeView< F, RealComposite< F > >.CompositeRandomAccess access = Views.collapseReal( features ).randomAccess();
		IntType classIndex = new IntType();
		Pair<Composite<? extends RealType<?>>, IntType> pair = new ValuePair<>(access.get(), classIndex);
		final Stream< Pair<Composite< ? extends RealType<?> >, ? extends IntegerType<?>> > featuresStream = IntStream.range(0, locations.length).mapToObj( i-> {
					classIndex.set(labels[i]);
					IntervalIndexer.indexToPosition(locations[i], features, access );
					return pair;
				}
		);
		return featuresStream.iterator();
	}

	public TrainClassifier(
			final MainFrame.Extensible extensible,
			final Classifier classifier,
			final TLongIntHashMap groundTruth,
			final RandomAccessibleInterval<F> features
	)
	{
		super( "Train Classifier" );
		this.classifier = classifier;
		this.groundTruth = groundTruth;
		this.features = features;
		extensible.addAction(this, "ctrl shift T");
	}

	private final Classifier classifier;

	private final RandomAccessibleInterval< F > features;

	private boolean trainingSuccess = false;

	public boolean getTrainingSuccess()
	{
		return trainingSuccess;
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
		trainingSuccess = false;
		final TLongIntHashMap samples = groundTruth;
		try
		{
			final long[] locations = samples.keys();
			final int[] labels = samples.values();
			classifier.trainClassifier( toSamples( features, locations, labels) );
			trainingSuccess = true;
		}
		catch ( final Exception e1 )
		{
			trainingSuccess = false;
		}
	}

}
