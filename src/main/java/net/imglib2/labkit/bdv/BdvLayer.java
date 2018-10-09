
package net.imglib2.labkit.bdv;

import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.utils.Notifier;

public interface BdvLayer {

	BdvShowable image();

	Notifier<Runnable> listeners();

	Holder<Boolean> visibility();

	String title();

	class FinalLayer implements BdvLayer {

		private final BdvShowable image;
		private final String title;
		private final Notifier<Runnable> listeners = new Notifier<>();
		private final Holder<Boolean> visibility;

		public FinalLayer(BdvShowable image, String title,
			Holder<Boolean> visibility)
		{
			this.image = image;
			this.title = title;
			this.visibility = visibility;
		}

		@Override
		public BdvShowable image() {
			return image;
		}

		@Override
		public Notifier<Runnable> listeners() {
			return listeners;
		}

		@Override
		public Holder<Boolean> visibility() {
			return visibility;
		}

		@Override
		public String title() {
			return title;
		}
	}
}
