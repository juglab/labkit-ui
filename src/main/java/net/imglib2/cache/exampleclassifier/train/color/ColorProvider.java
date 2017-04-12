package net.imglib2.cache.exampleclassifier.train.color;

import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.ARGBType;

public interface ColorProvider< S > extends Converter< S, ARGBType >
{

	public int getColor( S t );

	@Override
	default public void convert( final S s, final ARGBType t )
	{
		t.set( getColor( s ) );
	}

}
