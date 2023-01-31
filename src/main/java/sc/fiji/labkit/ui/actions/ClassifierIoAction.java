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

package sc.fiji.labkit.ui.actions;

import sc.fiji.labkit.ui.Extensible;
import sc.fiji.labkit.ui.models.SegmentationItem;
import sc.fiji.labkit.ui.models.SegmenterListModel;
import sc.fiji.labkit.ui.segmentation.SegmentationPlugin;
import sc.fiji.labkit.ui.segmentation.SegmentationPluginService;
import org.scijava.Context;

import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Implements the menu items for saving and opening the classifier.
 *
 * @author Matthias Arzt
 */
public class ClassifierIoAction extends AbstractFileIoAction {

	private final SegmenterListModel segmenterListModel;

	private final Context context;

	public ClassifierIoAction(Extensible extensible,
		final SegmenterListModel selectedSegmenter)
	{
		super(extensible, new FileNameExtensionFilter("Classifier", "classifier"));
		this.context = extensible.context();
		this.segmenterListModel = selectedSegmenter;
		initSaveAction(SegmentationItem.SEGMENTER_MENU, "Save Classifier ...", 101,
			this::save, "");
		initOpenAction(SegmentationItem.SEGMENTER_MENU, "Open Classifier ...", 100,
			this::open, "");
	}

	private void save(SegmentationItem item, String filename) {
		item.saveModel(filename);
	}

	private void open(SegmentationItem item, String filename) {
		item = addMatchingSegmentationItem(filename);
		item.openModel(filename);
		segmenterListModel.selectedSegmenter().set(item);
	}

	private SegmentationItem addMatchingSegmentationItem(String filename) {
		SegmentationPluginService pluginService = context.service(SegmentationPluginService.class);
		for (SegmentationPlugin plugin : pluginService.getSegmentationPlugins())
			if (plugin.canOpenFile(filename))
				return segmenterListModel.addSegmenter(plugin);
		throw new IllegalArgumentException("No suitable plugin found for opening: \"" + filename +
			"\"");
	}
}
