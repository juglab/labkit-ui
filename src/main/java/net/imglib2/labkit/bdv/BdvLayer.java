package net.imglib2.labkit.bdv;

import net.imglib2.labkit.utils.Notifier;
import net.imglib2.realtransform.AffineTransform3D;

public interface BdvLayer
{
	BdvShowable image();

	Notifier<Runnable> listeners();

	String title();

	AffineTransform3D transformation();

	class FinalLayer implements BdvLayer {

		private final BdvShowable image;
		private final String title;
		private final Notifier< Runnable > listeners = new Notifier<>();
		private final AffineTransform3D transformation;

		public FinalLayer( BdvShowable image, String title )
		{
			this.image = image;
			this.title = title;
			this.transformation = image.transformation();
		}

		@Override public BdvShowable image()
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
