package net.imglib2.labkit.plugin;

import bdv.util.AbstractSource;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.view.Views;

import java.util.List;

/**
 * {@link ResolutionPyramidSource} is a {@link bdv.viewer.Source}, that can be used to
 * display a resolution pyramid with {@link bdv.util.BdvFunctions}.
 */
public class ResolutionPyramidSource< T extends NumericType< T > > extends AbstractSource<T> {
	private final List< ? extends RandomAccessibleInterval< T > > resolutionPyramid;

	/**
	 * @param resolutionPyramid This must be a list of images. Sorted from high to low resolution.
	 * @param type Pixel type
	 * @param name Title of the image, to be displayed.
	 */
	public ResolutionPyramidSource(List<? extends RandomAccessibleInterval<T>> resolutionPyramid, T type, String name)
	{
		super(type, name);
		assert resolutionPyramid.stream().allMatch( source -> source.numDimensions() == 2 );
		this.resolutionPyramid = resolutionPyramid;
	}

	@Override public RandomAccessibleInterval<T> getSource( int t, int level )
	{
		// NB: sources are 2D images so, add an extra dimension to make them 3d as expected
		return Views.addDimension(resolutionPyramid.get(level), 0, 0);
	}

	@Override public int getNumMipmapLevels()
	{
		return resolutionPyramid.size();
	}

	@Override public void getSourceTransform( int t, int level, AffineTransform3D transform )
	{
		transform.identity();
		transform.scale( (double) resolutionPyramid.get(0).dimension( 0 ) / (double) resolutionPyramid.get(level).dimension( 0 )  );
	}
}
