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
import sc.fiji.labkit.ui.menu.MenuKey;
import sc.fiji.labkit.ui.segmentation.ForwardingSegmenter;
import sc.fiji.labkit.ui.segmentation.SegmentationPlugin;
import sc.fiji.labkit.ui.segmentation.Segmenter;
import net.imglib2.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A model that wraps around {@link Segmenter} and provides additional
 * information.
 */
public class SegmentationItem extends ForwardingSegmenter {

	public final static MenuKey<SegmentationItem> SEGMENTER_MENU = new MenuKey<>(
		SegmentationItem.class);

	private static final AtomicInteger counter = new AtomicInteger();

	private final String name;

	private String filename;

	private boolean modified;

	private final Map<ImageLabelingModel, SegmentationResultsModel> results;

	private final ExtensionPoints extensionPoints;

	public SegmentationItem(SegmentationPlugin plugin, ExtensionPoints extensionPoints) {
		super(plugin.createSegmenter());
		this.name = "#" + counter.incrementAndGet() + " - " + plugin.getTitle();
		this.extensionPoints = extensionPoints;
		this.results = new WeakHashMap<>();
		this.filename = null;
		this.modified = false;
	}

	@Deprecated
	public Segmenter segmenter() {
		return this;
	}

	public String name() {
		return name;
	}

	public SegmentationResultsModel results(ImageLabelingModel imageLabeling) {
		SegmentationResultsModel result = results.get(imageLabeling);
		if (result == null) {
			result = new SegmentationResultsModel(imageLabeling, extensionPoints, getSourceSegmenter());
			results.put(imageLabeling, result);
		}
		return result;
	}

	@Override
	public String toString() {
		return name();
	}

	@Override
	public void openModel(String path) {
		super.openModel(path);
		filename = path;
		modified = false;
		results.forEach((i, r) -> r.update());
	}

	@Override
	public void train(List<Pair<ImgPlus<?>, Labeling>> data) {
		results.forEach((i, r) -> r.clear());
		modified = true;
		super.train(data);
		results.forEach((i, r) -> r.update());
	}

	public boolean isModified() {
		return modified;
	}

	public String getFileName() {
		return filename;
	}
}
