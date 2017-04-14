package net.imglib2.atlas.classification;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

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
		public void notify( Classifier< Composite< F >, ?, ? > classifier, boolean trainingSuccess ) throws IOException;
	}

	public TrainClassifier(
			final Classifier< Composite< F >, ?, ? > classifier,
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

	private final Classifier< Composite< F >, ?, ? > classifier;

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
		try
		{
//			classifier.buildClassifier( instances );
//			synchronized ( samples )
//			{
			final long[] locations = samples.keys();
			final int[] labels = samples.values();
//			}
			final CompositeView< F, RealComposite< F > >.CompositeRandomAccess access = Views.collapseReal( features ).randomAccess();
			final Stream< Composite< F > > featuresStream = Arrays.stream( locations ).mapToObj( loc -> {
				IntervalIndexer.indexToPosition( loc, features, access );
				return access.get();
			} );
			classifier.trainClassifier( featuresStream::iterator, labels );
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
