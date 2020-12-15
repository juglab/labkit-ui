
package net.imglib2.labkit.bdv;

import bdv.util.BdvHandle;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.viewer.SynchronizedViewerState;
import bdv.viewer.ViewerStateChange;
import net.imglib2.Interval;
import net.imglib2.labkit.utils.properties.Property;
import net.imglib2.labkit.utils.Notifier;

import java.util.function.Consumer;

/**
 * BdvLayerLink links a {@link BdvLayer} to a Big Data Viewer
 * ({@link BdvHandle}).
 * <p>
 * The link involves for {@link BdvLayer#image()}, {@link BdvLayer#visibility()}
 * and {@link BdvLayer#listeners()}. The {@link BdvLayer#image() image} in
 * BdvLayer will be show in BDV and is updated whenever a change is indicated by
 * {@code BdvLayer.image().notifier()}. The {@link BdvLayer#visibility()
 * visibility} in BdvLayer is synchronized with the visibility of the respective
 * {@link BdvStackSource}. A call to {@link BdvLayer#listeners()} will trigger a
 * repaint of the BDV.
 */
public class BdvLayerLink implements Property<BdvStackSource<?>> {

	private final BdvHandle handle;

	private final SynchronizedViewerState viewerState;

	private final BdvLayer layer;

	private final Notifier notifier = new Notifier();

	private final Runnable onImageChanged = this::onImageChanged;

	private final Runnable onVisibilityChanged = this::onVisibilityChanged;

	private final Consumer<Interval> onRequestRepaint = this::onRequestRepaint;

	private BdvStackSource<?> bdvSource;

	public BdvLayerLink(BdvLayer layer, BdvHandle handle) {
		this.handle = handle;
		this.viewerState = handle.getViewerPanel().state();
		this.layer = layer;
		BdvOptions options = BdvOptions.options().addTo(handle);
		Property<BdvShowable> image = layer.image();
		BdvShowable showable1 = image.get();
		bdvSource = showable1 != null ? showable1.show(layer.title(), options) : null;
		image.notifier().addWeakListener(onImageChanged);
		layer.listeners().addWeakListener(onRequestRepaint);
		layer.visibility().notifier().addWeakListener(onVisibilityChanged);

		// NB: Listen to ViewerState changes, to get notified about visibility changes
		// in Big Data Viewer.
		viewerState.changeListeners().add(this::onViewerStateChanged);
	}

	private void onImageChanged() {
		BdvStackSource<?> source1 = bdvSource;
		bdvSource = null;
		if (source1 != null)
			source1.removeFromBdv();
		BdvShowable showable = layer.image().get();
		if (showable != null) {
			bdvSource = showable.show(layer.title(), BdvOptions.options().addTo(handle));
			bdvSource.setActive(layer.visibility().get());
		}
		onBdvSourceChanged();
		notifier.notifyListeners();
	}

	private void onRequestRepaint(Interval interval) {
		if (interval == null)
			handle.getViewerPanel().requestRepaint();
		else
			handle.getViewerPanel().requestRepaint(interval);
	}

	private void onBdvSourceChanged() {
		updateBdv();
	}

	private void onVisibilityChanged() {
		updateBdv();
	}

	private void updateBdv() {
		if (bdvSource != null)
			try
			{
				bdvSource.setActive(layer.visibility().get());
			}
			catch (NullPointerException ignore) {

			}
	}

	private void onViewerStateChanged(ViewerStateChange change) {
		BdvStackSource<?> source1 = bdvSource;
		if (source1 != null && change == ViewerStateChange.VISIBILITY_CHANGED) {
			boolean visible = viewerState.isSourceActive(source1.getSources().get(0));
			layer.visibility().set(visible);
		}
	}

	@Override
	public void set(BdvStackSource<?> value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public BdvStackSource<?> get() {
		return bdvSource;
	}

	@Override
	public Notifier notifier() {
		return notifier;
	}
}
