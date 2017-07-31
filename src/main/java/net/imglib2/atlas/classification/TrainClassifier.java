package net.imglib2.atlas.classification;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import gnu.trove.map.hash.TLongIntHashMap;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.atlas.control.brush.LabelBrushController;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.IntervalIndexer;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import net.imglib2.view.composite.CompositeView;
import net.imglib2.view.composite.RealComposite;

public class TrainClassifier< F extends RealType< F > > extends AbstractNamedAction
{

	public static interface Listener< F extends RealType< F > >
	{
		public void notify( Classifier classifier, boolean trainingSuccess ) throws IOException;
	}

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
			final Classifier classifier,
			final LabelBrushController controller,
			final RandomAccessibleInterval< F > features,
			final ArrayList< String > classes,
			final Listener... listeners
			)
	{
		super( "Train Classifier" );
		this.classifier = classifier;
		this.controller = controller;
		this.features = features;
		this.classes = classes;
		this.listeners = new ArrayList<>( Arrays.asList( listeners ) );
	}

	private final Classifier classifier;

	private final LabelBrushController controller;

	private final RandomAccessibleInterval< F > features;

	private final ArrayList< String > classes;

	private final ArrayList< Listener > listeners;

	private boolean trainingSuccess = false;

	public boolean getTrainingSuccess()
	{
		return trainingSuccess;
	}

	public void addListener( final Listener listener )
	{
		this.listeners.add( listener );
	}

	public boolean removeListener( final Listener listener )
	{
		return this.listeners.remove( listener );
	}

	public List< Listener > getListeners()
	{
		return listeners;
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
		trainingSuccess = false;
		final TLongIntHashMap samples = controller.getGroundTruth();
		try
		{
//			classifier.buildClassifier( instances );
//			synchronized ( samples )
//			{
			final long[] locations = samples.keys();
			final int[] labels = samples.values();
//			}
			classifier.trainClassifier( toSamples( features, locations, labels) );
			trainingSuccess = true;
		}
		catch ( final Exception e1 )
		{
			trainingSuccess = false;
		}
		listeners.forEach( l -> {

			try
			{
				l.notify( classifier, trainingSuccess );
			}
			catch ( final Exception e1 )
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} );
	}

}
