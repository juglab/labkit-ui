package net.imglib2.atlas.control.brush;

import java.util.Iterator;

import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;
import net.imglib2.RealLocalizable;
import net.imglib2.type.Type;

public interface PaintPixelsGenerator< T extends Type< T >, S extends Iterator< T > & Localizable >
{

	public S getPaintPixels( RandomAccessible< T > accessible, RealLocalizable position, int timestep, int size );

}
