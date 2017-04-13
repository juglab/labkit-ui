package net.imglib2.atlas.classification.weka;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.converter.AbstractConvertedRandomAccess;
import net.imglib2.converter.AbstractConvertedRandomAccessible;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.Composite;
import weka.core.Attribute;
import weka.core.Instance;

public class InstanceView< R extends RealType< R >, C extends Composite< R > > extends AbstractConvertedRandomAccessible< C, Instance >
{

	final RandomAccessible< C > source;

	private final Attribute[] attributes;

	public InstanceView( final RandomAccessible< C > source, final Attribute[] attributes )
	{
		super( source );
		this.source = source;
		this.attributes = attributes;
	}

	@Override
	public InstanceAccess< R, C > randomAccess()
	{
		return new InstanceAccess<>( source.randomAccess(), attributes );
	}

	@Override
	public InstanceAccess< R, C > randomAccess( final Interval interval )
	{
		return randomAccess();
	}

	public static class InstanceAccess< R extends RealType< R >, C extends Composite< R > > extends AbstractConvertedRandomAccess< C, Instance >
	{

		private final CompositeInstance< R, C > instance;

		private final Attribute[] attributes;


		public InstanceAccess( final RandomAccess< C > source, final Attribute[] attributes )
		{
			super( source );
			instance = new CompositeInstance< >( source.get(), attributes );
			this.attributes = attributes;
		}

		@Override
		public Instance get()
		{
			instance.setSource( source.get() );
			return instance;
		}

		@Override
		public InstanceAccess< R, C > copy()
		{
			return new InstanceAccess<>( source.copyRandomAccess(), attributes );
		}

	}

	// length of array is nFeatures + 1 ( for class attribute )
	// all attributes numeric, except class attribute (nominal)
	public static Attribute[] makeDefaultAttributes( final int nFeatures, final int nClasses )
	{
		final Attribute[] attributes = new Attribute[ nFeatures + 1 ];
		for ( int i = 0; i < nFeatures; ++i )
			attributes[ i ] = new Attribute( "" + i );
		attributes[ nFeatures ] = new Attribute( "class", IntStream.range( 0, nClasses ).mapToObj( i -> "" + i ).collect( Collectors.toList() ) );
		return attributes;
	}


}
