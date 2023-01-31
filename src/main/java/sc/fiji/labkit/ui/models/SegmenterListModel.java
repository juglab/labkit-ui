/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.ui.models;

import net.imagej.ImgPlus;
import sc.fiji.labkit.ui.labeling.Labeling;
import sc.fiji.labkit.ui.segmentation.SegmentationPlugin;
import net.imglib2.util.Pair;
import org.scijava.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of {@link SegmentationItem}, with selected item, and a bunch of
 * listeners.
 */
public class SegmenterListModel {

	private final Context context;
	private final ExtensionPoints extensionPoints;
	private final Holder<List<SegmentationItem>> segmenters = new DefaultHolder<>(new ArrayList<>());
	private final Holder<SegmentationItem> selectedSegmenter = new DefaultHolder<>(null);
	private final Holder<Boolean> segmentationVisibility = new DefaultHolder<>(true);
	private final Holder<List<Pair<ImgPlus<?>, Labeling>>> trainingData = new DefaultHolder<>(null);

	public SegmenterListModel(Context context, ExtensionPoints extensionPoints) {
		this.context = context;
		this.extensionPoints = extensionPoints;
		this.segmenters.notifier().addListener(() -> {
			if (!segmenters.get().contains(selectedSegmenter.get())) selectedSegmenter.set(null);
		});
	}

	public Holder<List<SegmentationItem>> segmenters() {
		return segmenters;
	}

	public Holder<SegmentationItem> selectedSegmenter() {
		return selectedSegmenter;
	}

	public SegmentationItem addSegmenter(SegmentationPlugin plugin) {
		SegmentationItem segmentationItem = new SegmentationItem(plugin, extensionPoints);
		segmenters.get().add(segmentationItem);
		segmenters.notifier().notifyListeners();
		return segmentationItem;
	}

	public void remove(SegmentationItem item) {
		segmenters.get().remove(item);
		segmenters.notifier().notifyListeners();
	}

	public Holder<Boolean> segmentationVisibility() {
		return segmentationVisibility;
	}

	public Context context() {
		return context;
	}

	public Holder<List<Pair<ImgPlus<?>, Labeling>>> trainingData() {
		return trainingData;
	}
}
