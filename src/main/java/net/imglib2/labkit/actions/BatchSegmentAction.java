
package net.imglib2.labkit.actions;

import net.imglib2.labkit.BatchSegmenter;
import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.MenuBar;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.utils.progress.StatusServiceProgressWriter;
import org.scijava.Cancelable;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.widget.FileWidget;

import java.io.File;

/**
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
			ignore -> ((Runnable) this::segmentImages).run(), null, "");
	}

	private void segmentImages() {
		CommandService command = extensible.context().service(CommandService.class);
		command.run(BatchSegment.class, true, "segmenter", selectedSegmenter.get()
			.segmenter());
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
