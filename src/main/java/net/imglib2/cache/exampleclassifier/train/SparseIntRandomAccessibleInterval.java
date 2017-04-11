package net.imglib2.cache.exampleclassifier.train;

import gnu.trove.map.hash.TLongIntHashMap;
import net.imglib2.AbstractInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.IntegerType;

public class SparseIntRandomAccessibleInterval< I extends IntegerType< I > > extends AbstractInterval implements RandomAccessibleInterval< I >
{

	private final TLongIntHashMap values;

	private final I type;

	private final int background;

	public SparseIntRandomAccessibleInterval( final TLongIntHashMap values, final Interval interval, final I type, final int background )
	{
		super( interval );
		this.values = values;
		this.type = type;
		this.background = background;
	}

	@Override
	public SparseIntRandomAccess< I > randomAccess()
	{
//		synchronized ( values )
		{
			return new SparseIntRandomAccess<>( values, this, type, background );
		}
	}

	@Override
	public SparseIntRandomAccess< I > randomAccess( final Interval interval )
	{
		return randomAccess();
	}

}
