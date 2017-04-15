package net.imglib2.atlas.classification.weka;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.imglib2.RandomAccessibleInterval;
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

public class WekaClassifier< R extends RealType< R >, I extends IntegerType< I > >
implements Classifier< Composite< R >, RandomAccessibleInterval< R >, RandomAccessibleInterval< I > >
{

	private final weka.classifiers.Classifier classifier;

	private List< String > classLabels;

	private int numFeatures;

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
	public void predictLabels( final RandomAccessibleInterval< R > instances, final RandomAccessibleInterval< I > labels ) throws Exception
	{
		final InstanceView< R, RealComposite< R > > wekaInstances = new InstanceView<>( Views.collapseReal( instances ), InstanceView.makeDefaultAttributes( numFeatures, classLabels.size() ) );
		for ( final Pair< Instance, I > p : Views.interval( Views.pair( wekaInstances, labels ), labels ) )
			p.getB().setInteger( ( int ) classifier.classifyInstance( p.getA() ) );

	}

	@Override
	public void trainClassifier( final Iterable< Composite< R > > samples, final int[] labels ) throws Exception
	{
		final ArrayList< Attribute > attributes = new ArrayList<>();
		for ( int i = 0; i < numFeatures; ++i )
			attributes.add( new Attribute( "" + i ) );
		attributes.add( new Attribute( "class", classLabels ) );
		final int nSamples = labels.length;
		final Instances instances = new Instances( "training", attributes, nSamples );
		instances.setClassIndex( numFeatures );

		final Iterator< Composite< R > > it = samples.iterator();
		System.out.println( "Collecting training examples. " + nSamples );
		for ( int i = 0; it.hasNext(); ++i )
		{
			final Composite< R > feat = it.next();
			final int label = labels[ i ];
			final double[] values = new double[ numFeatures + 1 ];
			for ( int f = 0; f < numFeatures; ++f )
				values[ f ] = feat.get( f ).getRealDouble();
			values[ numFeatures ] = label;
			instances.add( new DenseInstance( 1.0, values ) );
		}
		System.out.println( "Starting training!" );
		classifier.buildClassifier( instances );
		System.out.println( "Training successful!" );
	}

}
