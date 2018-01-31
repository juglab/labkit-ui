package net.imglib2.labkit.labeling;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;

public interface BdvLayer
{
	RandomAccessibleInterval< ? extends NumericType< ? > > image();

	Notifier<Runnable> listeners();

	String title();

	AffineTransform3D transformation();

	class FinalLayer implements BdvLayer {

		private final RandomAccessibleInterval< ? extends NumericType< ? > > image;
		private final String title;
		private final Notifier< Runnable > listeners = new Notifier<>();
		private final AffineTransform3D transformation;

		public FinalLayer( RandomAccessibleInterval< ARGBType > image, String title )
		{
			this(image, title, new AffineTransform3D());
		}

		public FinalLayer( RandomAccessibleInterval< ? extends NumericType< ? > > image, String title, AffineTransform3D transformation )
		{
			this.image = image;
			this.title = title;
			this.transformation = transformation;
		}

		@Override public RandomAccessibleInterval< ? extends NumericType< ? > > image()
		{
			return image;
		}

		@Override public Notifier< Runnable > listeners()
		{
			return listeners;
		}

		@Override public String title()
		{
			return title;
		}

		@Override public AffineTransform3D transformation()
		{
			return transformation;
		}
	}
}
