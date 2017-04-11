package net.imglib2.cache.exampleclassifier.train;

import org.scijava.ui.behaviour.ScrollBehaviour;

import bdv.viewer.ViewerPanel;
import net.imglib2.RandomAccessibleInterval;

public class MouseWheelSelector implements ScrollBehaviour
{

	private final MouseWheelSelectorRandomAccessibleInterval< ? > rai;

	private final ViewerPanel viewer;

	public MouseWheelSelector( final RandomAccessibleInterval< ? > rai, final int d, final ViewerPanel viewer )
	{
		this( new MouseWheelSelectorRandomAccessibleInterval<>( rai, d ), viewer );
	}

	public MouseWheelSelector( final MouseWheelSelectorRandomAccessibleInterval< ? > rai, final ViewerPanel viewer )
	{
		super();
		this.rai = rai;
		this.viewer = viewer;
	}

	@Override
	public void scroll( final double wheelRotation, final boolean isHorizontal, final int x, final int y )
	{
		if ( !isHorizontal )
			synchronized( viewer ) {
				if ( wheelRotation < 0 )
					rai.setsSlice( Math.min( rai.getSliceIndex() + 1, rai.getMaxSlice() ) );
				else if ( wheelRotation > 0 )
					rai.setsSlice( Math.max( rai.getSliceIndex() - 1, rai.getMinSlice() ) );

				viewer.requestRepaint();
			}
	}

}
