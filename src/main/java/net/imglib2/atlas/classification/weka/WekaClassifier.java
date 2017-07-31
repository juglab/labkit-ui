package net.imglib2.atlas.classification.weka;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.InstanceView;
import net.imglib2.atlas.classification.Classifier;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import net.imglib2.view.composite.RealComposite;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class WekaClassifier< R extends RealType< R >, I extends IntegerType< I > >
implements Classifier
{

	public static class NotTrainedYet extends Exception
	{

	}

	private weka.classifiers.Classifier classifier;

	private List< String > classLabels;

	private int numFeatures;

	private boolean classifierTrainedSuccessfully;

	public WekaClassifier( final weka.classifiers.Classifier classifier, final List< String > classLabels, final int numFeatures )
	{
		super();
		this.classifier = classifier;
		this.classLabels = classLabels;
		this.numFeatures = numFeatures;
	}

	public void setNumFeatures( final int numFeatures )
	{
		this.numFeatures = numFeatures;
	}

	public void setClasses( final List< String > classLabels )
	{
		this.classLabels = classLabels;
	}

	public void setNumFeaturesAndClasses( final int numFeatures, final List< String > classLabels )
	{
		setNumFeatures( numFeatures );
		setClasses( classLabels );
	}

	@Override
	public void predictLabels(RandomAccessibleInterval<? extends Composite<? extends RealType<?>>> instances, RandomAccessibleInterval<? extends IntegerType<?>> labels) throws Exception
	{
		InstanceView<? extends Composite<? extends RealType<?>>> wekaInstances = new InstanceView<>(instances, InstanceView.makeDefaultAttributes(numFeatures, classLabels.size()));
		for ( final Pair< Instance, ? extends IntegerType<?> > p : Views.interval( Views.pair( wekaInstances, labels ), labels ) )
			p.getB().setInteger( ( int ) classifier.classifyInstance( p.getA() ) );

	}

	@Override
	public void trainClassifier(Iterator<Pair<Composite<? extends RealType<?>>, ? extends IntegerType<?>>> data) throws Exception
	{
		final ArrayList< Attribute > attributes = new ArrayList<>();
		for ( int i = 0; i < numFeatures; ++i )
			attributes.add( new Attribute( "" + i ) );
		attributes.add( new Attribute( "class", classLabels ) );
		final Instances instances = new Instances( "training", attributes, 0 );
		instances.setClassIndex( numFeatures );

		System.out.print( "Collecting training examples. " );
		while ( data.hasNext() )
		{
			Pair<Composite<? extends RealType<?>>, ? extends IntegerType<?>> pair = data.next();
			final Composite< ? extends RealType<?> > feat = pair.getA();
			final int label = pair.getB().getInteger();
			final double[] values = new double[ numFeatures + 1 ];
			for ( int f = 0; f < numFeatures; ++f )
				values[ f ] = feat.get( f ).getRealDouble();
			values[ numFeatures ] = label;
			instances.add( new DenseInstance( 1.0, values ) );
		}
		synchronized ( this )
		{
			System.out.println( "Starting training!" );
			classifier.buildClassifier( instances );
			System.out.println( "Training successful!" );
			this.classifierTrainedSuccessfully = true;
		}
	}

	@Override
	public boolean isTrained() {
		return this.classifierTrainedSuccessfully;
	}

	@Override
	synchronized public void saveClassifier( final String path, final boolean overwrite ) throws Exception
	{
		{
			if ( !classifierTrainedSuccessfully )
				throw new NotTrainedYet();
			if ( new File( path ).exists() && !overwrite )
				throw new FileAlreadyExistsException( path );
			SerializationHelper.write( path, this.classifier );
		}
	}

	@Override
	public void loadClassifier( final String path ) throws Exception
	{
		synchronized ( classifier )
		{
			this.classifier = ( weka.classifiers.Classifier ) SerializationHelper.read( path );
			this.classifierTrainedSuccessfully = true;
		}
	}

}
