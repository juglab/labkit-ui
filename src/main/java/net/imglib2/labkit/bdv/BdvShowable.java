package net.imglib2.labkit.bdv;

import mpicbg.spim.data.SpimData;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.NumericType;

import java.util.Objects;

public class BdvShowable
{

	private final RandomAccessibleInterval< ? extends NumericType<?> > image;
	private final SpimData spimData;

	private BdvShowable( RandomAccessibleInterval< ? extends NumericType< ? > > image, SpimData spimData )
	{
		this.image = image;
		this.spimData = spimData;
	}

	public static BdvShowable wrap( RandomAccessibleInterval< ? extends NumericType< ? > > image )
	{
		return new BdvShowable( Objects.requireNonNull( image ), null );
	}

	public static BdvShowable wrap( SpimData spimData )
	{
		return new BdvShowable( null, Objects.requireNonNull( spimData ) );
	}

	public boolean isSpimData() {
		return false;
	}

	public RandomAccessibleInterval< ? extends NumericType< ? > > image() {
		if(image == null)
			throw new IllegalStateException( "" );
		return image;
	}

	public SpimData spimData() {
		if(spimData == null)
			throw new IllegalStateException( "" );
		return spimData;
	}
}
