
package net.imglib2.labkit.bdv;

import net.imglib2.Interval;
import net.imglib2.labkit.utils.properties.Property;
import net.imglib2.labkit.utils.ParametricNotifier;

/**
 * Objects that implement {@link BdvLayer}, can easily be made visible in
 * BigDataViewer using {@link BdvLayerLink}.
 */
public interface BdvLayer {

	Property<BdvShowable> image();

	ParametricNotifier<Interval> listeners();

	Property<Boolean> visibility();

	String title();

	class FinalLayer implements BdvLayer {

		private final Property<BdvShowable> image;
		private final String title;
		private final ParametricNotifier<Interval> listeners =
			new ParametricNotifier<>();
		private final Property<Boolean> visibility;

		public FinalLayer(Property<BdvShowable> image, String title,
			Property<Boolean> visibility)
		{
			this.image = image;
			this.title = title;
			this.visibility = visibility;
		}

		@Override
		public Property<BdvShowable> image() {
			return image;
		}

		@Override
		public ParametricNotifier<Interval> listeners() {
			return listeners;
		}

		@Override
		public Property<Boolean> visibility() {
			return visibility;
		}

		@Override
		public String title() {
			return title;
		}
	}
}
