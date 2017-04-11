package net.imglib2.cache.exampleclassifier.train;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.scijava.ui.behaviour.util.AbstractNamedAction;

import gnu.trove.iterator.TLongIntIterator;
import gnu.trove.map.hash.TLongIntHashMap;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.IntervalIndexer;
import net.imglib2.view.Views;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.RealComposite;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class TrainClassifier< F extends RealType< F > > extends AbstractNamedAction
{

	public static interface Listener
	{
		public void notify( Classifier classifier, boolean trainingSuccess ) throws IOException;
	}

	public TrainClassifier(
			final Classifier classifier,
			final LabelBrushController controller,
			final RandomAccessibleInterval< F > features,
			final ArrayList< String > classes,
			final Listener... listeners
			)
	{
		super( "train classifier" );
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

	@Override
	public void actionPerformed( final ActionEvent e )
	{
		trainingSuccess = false;
		final TLongIntHashMap samples = controller.getGroundTruth();
		final int numFeatures = ( int ) features.dimension( features.numDimensions() - 1 );
		final ArrayList< Attribute > attributes = new ArrayList<>();
		for ( int i = 0; i < numFeatures; ++i )
			attributes.add( new Attribute( "" + i ) );
		attributes.add( new Attribute( "class", classes ) );

		final int nSamples = samples.size();

		final Instances instances = new Instances( "training", attributes, nSamples );
		instances.setClassIndex( numFeatures );
		final CompositeIntervalView< F, RealComposite< F > > collapsedFeatures = Views.collapseReal( features );
		final RandomAccess< RealComposite< F > > featAccess = collapsedFeatures.randomAccess();

		System.out.println( "Collecting training examples." );
//		for ( int i = 1; i <= classes.size(); ++i )
		for ( final TLongIntIterator it = samples.iterator(); it.hasNext(); )
		{
			it.advance();
			final long pos = it.key();
			IntervalIndexer.indexToPosition( pos, collapsedFeatures, featAccess );
			final int label = it.value() - 1;
			final RealComposite< F > feat = featAccess.get();
			final double[] values = new double[ numFeatures + 1 ];
			for ( int f = 0; f < numFeatures; ++f )
				values[ f ] = feat.get( f ).getRealDouble();
			values[ numFeatures ] = label;
			instances.add( new DenseInstance( 1.0, values ) );
		}
		System.out.println( "Starting training!" );
		try
		{
			classifier.buildClassifier( instances );
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
