package net.imglib2.labkit.models;

import bdv.viewer.ViewerPanel;
import net.imglib2.Interval;
import net.imglib2.realtransform.AffineTransform3D;

import java.util.Arrays;
import java.util.Collections;

public class TransformationModel
{
	private ViewerPanel viewerPanel;

	public void initialize(ViewerPanel viewerPanel) {
		this.viewerPanel = viewerPanel;
	}

	public int width()
	{
		return viewerPanel == null ? 100 : viewerPanel.getWidth();
	}

	public int height()
	{
		return viewerPanel == null ? 100 : viewerPanel.getHeight();
	}

	public void setTransformation( AffineTransform3D transformation )
	{
		if(viewerPanel != null)
			viewerPanel.setCurrentViewerTransform( transformation );
	}

	public void transformToShowInterval( Interval interval ) {
		final double[] screenSize = { width(), height() };
		setTransformation( getTransformation( interval, screenSize ) );
	}

	private static AffineTransform3D getTransformation( Interval interval, double[] screenSize )
	{
		final double scale = 0.5 * getBiggestScaleFactor( screenSize, interval );
		final double[] translate = getTranslation( screenSize, interval, scale );
		final AffineTransform3D transform = new AffineTransform3D();
		transform.scale( scale );
		transform.translate( translate );
		return transform;
	}

	private static double[] getTranslation( final double[] screenSize, final Interval labelBox, final double labelScale ) {
		final double[] translate = new double[ 3 ];
		for ( int i = 0; i < Math.min( translate.length, labelBox.numDimensions() ); i++ ) {
			translate[ i ] = - ( labelBox.min( i ) + labelBox.max( i ) ) * labelScale / 2;
			if ( i < 2 ) {
				translate[ i ] += screenSize[ i ] / 2;
			}
		}
		return translate;
	}

	private static double getBiggestScaleFactor( final double[] screenSize, final Interval labelBox ) {
		final Double[] scales = new Double[ 2 ];
		final double minLength = 20.0;
		for ( int i = 0; i < 2; i++ )
			scales[ i ] = screenSize[ i ] / Math.max( labelBox.max( i ) - labelBox.min( i ), minLength );
		return Collections.min( Arrays.asList( scales ) );
	}
}
