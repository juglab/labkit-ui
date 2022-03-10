/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2022 Matthias Arzt
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

import sc.fiji.labkit.ui.BatchSegmenter;
import sc.fiji.labkit.ui.Extensible;
import sc.fiji.labkit.ui.MenuBar;
import sc.fiji.labkit.ui.models.Holder;
import sc.fiji.labkit.ui.models.SegmentationItem;
import sc.fiji.labkit.ui.segmentation.Segmenter;
import sc.fiji.labkit.ui.utils.progress.StatusServiceProgressWriter;
import org.scijava.Cancelable;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.widget.FileWidget;

import java.io.File;

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
	}

	private void segmentImages() {
		CommandService command = extensible.context().service(CommandService.class);
		command.run(BatchSegment.class, true, "segmenter", selectedSegmenter.get());
	}

	public static class BatchSegment implements Command, Cancelable {

		@Parameter(label = "input directory", style = FileWidget.DIRECTORY_STYLE)
		private File inputDirectory;

		@Parameter(label = "output directory", style = FileWidget.DIRECTORY_STYLE)
		private File outputDirectory;

		@Parameter
		private StatusService statusService;

		@Parameter
		private Segmenter segmenter;

		public void setSegmenter(Segmenter segmenter) {
			this.segmenter = segmenter;
		}

		@Override
		public void run() {
			BatchSegmenter batchSegmenter = new BatchSegmenter(segmenter,
				new StatusServiceProgressWriter(statusService));
			for (File file : inputDirectory.listFiles())
				if (file.isFile()) processFile(batchSegmenter, file);
		}

		private void processFile(BatchSegmenter batchSegmenter, File file) {
			File outputFile = new File(outputDirectory, file.getName());
			try {
				batchSegmenter.segment(file.getAbsoluteFile(), outputFile
					.getAbsoluteFile());
			}
			catch (Exception e) {
				System.err.print(e);
			}
		}

		@Override
		public boolean isCanceled() {
			return false;
		}

		@Override
		public void cancel(String reason) {

		}

		@Override
		public String getCancelReason() {
			return null;
		}
	}
}
