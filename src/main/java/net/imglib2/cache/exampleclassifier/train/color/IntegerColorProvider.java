package net.imglib2.cache.exampleclassifier.train.color;

import net.imglib2.type.numeric.IntegerType;

public interface IntegerColorProvider< I extends IntegerType< I > > extends ColorProvider< I >
{
	default public int getColor( final int i )
	{
		return getColor( ( long ) i );
	}

	public int getColor( long l );

	@Override
	default public int getColor( final I i )
	{
		return getColor( i.getIntegerLong() );
	}

}
