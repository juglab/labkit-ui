package net.imglib2.cache.exampleclassifier.train.color;

import net.imglib2.Volatile;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.volatiles.VolatileARGBType;

public class IntegerARGBConverters
{

	public static class ARGB< I extends IntegerType< I > > implements Converter< I, ARGBType >
	{
		private final IntegerColorProvider provider;

		public ARGB( final IntegerColorProvider provider )
		{
			super();
			this.provider = provider;
		}

		@Override
		public void convert( final I input, final ARGBType output )
		{
			output.set( provider.getColor( input.getIntegerLong() ) );
		}
	}

	public static class VolatileARGB< I extends IntegerType< I > > implements Converter< Volatile< I >, VolatileARGBType >
	{
		private final IntegerColorProvider provider;

		public VolatileARGB( final IntegerColorProvider provider )
		{
			super();
			this.provider = provider;
		}

		@Override
		public void convert( final Volatile< I > input, final VolatileARGBType output )
		{
			final boolean isValid = input.isValid();
			output.setValid( isValid );
			if ( isValid )
				output.set( provider.getColor( input.get().getIntegerLong() ) );
		}
	}

}
