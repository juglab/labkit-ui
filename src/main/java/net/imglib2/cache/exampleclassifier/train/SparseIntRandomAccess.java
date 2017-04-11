package net.imglib2.cache.exampleclassifier.train;

import gnu.trove.map.hash.TLongIntHashMap;
import net.imglib2.Interval;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.util.IntervalIndexer;

public class SparseIntRandomAccess< I extends IntegerType< I > > extends Point implements RandomAccess< I >
{

	private final TLongIntHashMap values;

	private final Interval interval;

	private final I type;

	private final int background;

	public SparseIntRandomAccess( final TLongIntHashMap values, final Interval interval, final I type, final int background )
	{
		super( interval.numDimensions() );
		this.values = values;
		this.interval = interval;
		this.type = type;
		this.background = background;
	}

	@Override
	public I get()
	{
		final long index = IntervalIndexer.positionToIndex( this, interval );
		type.setInteger( values.contains( index ) ? values.get( index ) : background );
		return type;
	}

	@Override
	public SparseIntRandomAccess< I > copy()
	{
		return copyRandomAccess();
	}

	@Override
	public SparseIntRandomAccess< I > copyRandomAccess()
	{
		final SparseIntRandomAccess< I > other = new SparseIntRandomAccess<>( values, interval, type.copy(), background );
		other.setPosition( this );
		return other;
	}

}
