package net.imglib2.labkit.segmentation.weka;

import java.util.Enumeration;

import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.Composite;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class CompositeInstance< R extends RealType< R >, C extends Composite< R > > implements Instance
{

	// Store RealComposite< R > instead of Sampler< RealComposite< R > >
	// This requires a setSource method but for getting attributes is only
	// RealComposite.get instead of Sampler.get.get
	private C source;

	private final Attribute[] attributes;

	private final int classIndex;


	public CompositeInstance( final C source, final Attribute[] attributes )
	{
		super();
		this.source = source;
		this.attributes = attributes;
		this.classIndex = attributes.length - 1;
	}

	public void setSource( final C source )
	{
		this.source = source;
	}

	@Override
	public CompositeInstance< R, C > copy()
	{
		return new CompositeInstance<>( source, attributes );
	}

	@Override
	public double value( final int attIndex )
	{
		return source.get( attIndex ).getRealDouble();
	}

	@Override
	public double weight()
	{
		// TODO allow non-unit weights?
		return 1.0;
	}

	@Override
	public int classIndex()
	{
		return classIndex;
	}

	@Override
	public Attribute classAttribute()
	{
		return attributes[ classIndex ];
	}

	// class attribute: What is the space of predicted class, e.g. numeric
	// (regression)

	// for RandomForest prediction: Do I only need Instance.value(int)?
	// Should all attributes be numeric (except class attribute)?

	@Override
	public Attribute attribute( final int index )
	{
		return attributes[ index ];
	}

	@Override
	public int numAttributes()
	{
		return attributes.length;
	}

	@Override
	public int numClasses()
	{
		// TODO Auto-generated method stub
		return attributes[ classIndex ].numValues();
	}

	//
	//
	// anything below I do not need to implement?
	//
	//

	@Override
	public int numValues()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Attribute attributeSparse( final int indexOfIndex )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean classIsMissing()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double classValue()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Instance copy( final double[] values )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Instances dataset()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteAttributeAt( final int position )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Enumeration< Attribute > enumerateAttributes()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean equalHeaders( final Instance inst )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String equalHeadersMsg( final Instance inst )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasMissingValue()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int index( final int position )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void insertAttributeAt( final int position )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isMissing( final int attIndex )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isMissingSparse( final int indexOfIndex )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isMissing( final Attribute att )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Instance mergeInstance( final Instance inst )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void replaceMissingValues( final double[] array )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setClassMissing()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setClassValue( final double value )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setClassValue( final String value )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setDataset( final Instances instances )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setMissing( final int attIndex )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setMissing( final Attribute att )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setValue( final int attIndex, final double value )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setValueSparse( final int indexOfIndex, final double value )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setValue( final int attIndex, final String value )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setValue( final Attribute att, final double value )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setValue( final Attribute att, final String value )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setWeight( final double weight )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Instances relationalValue( final int attIndex )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Instances relationalValue( final Attribute att )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String stringValue( final int attIndex )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String stringValue( final Attribute att )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] toDoubleArray()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toStringNoWeight( final int afterDecimalPoint )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toStringNoWeight()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toStringMaxDecimalDigits( final int afterDecimalPoint )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString( final int attIndex, final int afterDecimalPoint )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString( final int attIndex )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString( final Attribute att, final int afterDecimalPoint )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString( final Attribute att )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double valueSparse( final int indexOfIndex )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double value( final Attribute att )
	{
		// TODO Auto-generated method stub
		return 0;
	}

}
