
package net.imglib2.labkit.bdv;

import bdv.util.BdvHandle;
import bdv.util.BdvStackSource;
import bdv.viewer.SynchronizedViewerState;
import bdv.viewer.ViewerStateChange;
import net.imglib2.labkit.models.Holder;

import java.lang.ref.WeakReference;

/**
 * Keeps a give {@code Holder<Boolean> visibility} synchronized, with the
 * visibility of a given {@code Holder<BdvStackSource<?>>} source.
 */
public class BdvVisibilityLink {

	private final Holder<BdvStackSource<?>> source;

	private final Holder<Boolean> visibility;

	private final SynchronizedViewerState viewerState;

	private final Runnable visibilityChanged = this::visibilityChanged;

	public BdvVisibilityLink(Holder<Boolean> visibility, BdvHandle bdvHandle,
		Holder<BdvStackSource<?>> source)
	{
		this.source = source;
		this.visibility = visibility;
		this.viewerState = bdvHandle.getViewerPanel().state();
		this.source.notifier().addListener(this::sourceChange);

		// NB: Only use weak listener here. This allows for the BdvStackSource, (and the
		// image) to be garbage collected, even if the Holder<Boolean> visibility
		// remains used.
		visibility.notifier().addWeakListener(visibilityChanged);

		// NB: Listen to ViewerState changes, to get notified about visibility changes
		// in Big Data Viewer.
		viewerState.changeListeners().add(this::viewerStateChanged);
	}

	private void sourceChange() {
		BdvStackSource<?> source = this.source.get();
		if (source != null)
			try
			{
				source.setActive(this.visibility.get());
			}
			catch (NullPointerException ignore) {

			}
	}

	private void visibilityChanged() {
		BdvStackSource<?> source = this.source.get();
		if (source != null)
			try
			{
				source.setActive(this.visibility.get());
			}
			catch (NullPointerException ignore) {

			}
	}

	private void viewerStateChanged(ViewerStateChange change) {
		BdvStackSource<?> source1 = source.get();
		if (source1 != null && change == ViewerStateChange.VISIBILITY_CHANGED) {
			boolean visible = viewerState.isSourceActive(source1.getSources().get(0));
			visibility.set(visible);
		}
	}
}
