package net.imglib2.atlas.color;

public interface IntegerColorProvider
{
	default public int getColor( final int i )
	{
		return getColor( ( long ) i );
	}

	public int getColor( long l );

}
