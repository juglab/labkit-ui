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
import sc.fiji.labkit.ui.MenuBar;
import sc.fiji.labkit.ui.models.Holder;
import sc.fiji.labkit.ui.models.SegmentationItem;
import sc.fiji.labkit.ui.plugin.LabkitProbabilityMapForImagesInDirectoryPlugin;
import sc.fiji.labkit.ui.plugin.LabkitSegmentImagesInDirectoryPlugin;
import org.scijava.command.CommandService;

import java.io.IOException;
import java.nio.file.Files;

/**
 * Implements the "Batch Segment Images ..." menu item.
 *
 * @author Matthias Arzt
 */
public class BatchSegmentAction {

	private final Extensible extensible;
	private final Holder<SegmentationItem> selectedSegmenter;

	public BatchSegmentAction(Extensible extensible,
		Holder<SegmentationItem> selectedSegmenter)
	{
		this.extensible = extensible;
		this.selectedSegmenter = selectedSegmenter;
		extensible.addMenuItem(MenuBar.OTHERS_MENU, "Batch Segment Images ...", 0,
			ignore -> segmentImages(), null, "");
		extensible.addMenuItem(MenuBar.OTHERS_MENU, "Batch Calculate Probability Maps for Images ...",
			1, ignore -> calculateProbibilityMaps(), null, "");
	}

	private void segmentImages() {
		CommandService command = extensible.context().service(CommandService.class);
		command.run(LabkitSegmentImagesInDirectoryPlugin.class, true,
			"segmenter_file", createClassifierFile());
	}

	private void calculateProbibilityMaps() {
		CommandService command = extensible.context().service(CommandService.class);
		command.run(LabkitProbabilityMapForImagesInDirectoryPlugin.class, true,
			"segmenter_file", createClassifierFile());
	}

	private String createClassifierFile() {
		try {
			SegmentationItem segmenter = selectedSegmenter.get();
			if (!segmenter.isModified())
				return segmenter.getFileName();
			String tmp = Files.createTempFile("labkit-pixel-classifier", ".classifier").toFile()
				.toString();
			segmenter.saveModel(tmp);
			return tmp;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
