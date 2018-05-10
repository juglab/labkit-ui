package net.imglib2.labkit.actions;

import net.imglib2.labkit.BatchSegmenter;
import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.trainable_segmention.RevampUtils;
import org.scijava.Cancelable;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.command.CommandInfo;
import org.scijava.module.Module;
import org.scijava.module.ModuleException;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginService;
import org.scijava.ui.swing.widget.SwingInputHarvester;
import org.scijava.widget.FileWidget;
import org.scijava.widget.InputHarvester;

import javax.swing.*;
import java.io.File;
import java.util.List;

/**
 * @author Matthias Arzt
 */
public class BatchSegmentAction {

	private final Extensible extensible;
	private final Holder<SegmentationItem> selectedSegmenter;

	public BatchSegmentAction(Extensible extensible, Holder< SegmentationItem > selectedSegmenter ) {
		this.extensible = extensible;
		this.selectedSegmenter = selectedSegmenter;
		extensible.addAction("Batch Segment Images ...", "batchSegment", this::segmentImages, "");
	}

	private void segmentImages() {
		BatchSegment commandInstance = new BatchSegment();
		commandInstance.setSegmenter( selectedSegmenter.get().segmenter() );
		boolean success = harvest(commandInstance, "Batch Segment Images");
		if(success) commandInstance.run();
	}

	private static boolean harvest(Command commandInstance, String title) {
		CommandInfo ci = new CommandInfo(commandInstance.getClass());
		Module module = ci.createModule(commandInstance);
		ci.setLabel(title);
		try {
			getHarvester(new Context()).harvest(module);
		} catch (ModuleException e) {
			return false;
		}
		return true;
	}

	private static InputHarvester<JPanel, JPanel> getHarvester(Context context) {
		List<InputHarvester> harvester1 = RevampUtils.filterForClass(InputHarvester.class,
				context.service(PluginService.class).createInstancesOfType(PreprocessorPlugin.class));
		List<SwingInputHarvester> swing = RevampUtils.filterForClass(SwingInputHarvester.class, harvester1);
		return swing.isEmpty() ? harvester1.get(0) : swing.get(0);
	}

	public static class BatchSegment implements Command, Cancelable {

		@Parameter(label = "input directory", style = FileWidget.DIRECTORY_STYLE)
		private File inputDirectory;

		@Parameter(label = "output directory", style = FileWidget.DIRECTORY_STYLE)
		private File outputDirectory;

		@Parameter
		private StatusService statusService;

		private Segmenter segmenter;

		public void setSegmenter(Segmenter segmenter ) {
			this.segmenter = segmenter;
		}

		@Override
		public void run() {
			BatchSegmenter batchSegmenter = new BatchSegmenter( segmenter, statusService::showProgress );
			for(File file : inputDirectory.listFiles())
				if(file.isFile())
					processFile(batchSegmenter, file);
		}

		private void processFile(BatchSegmenter batchSegmenter, File file) {
			File outputFile = new File(outputDirectory, file.getName());
			try {
				batchSegmenter.segment(file.getAbsoluteFile(), outputFile.getAbsoluteFile());
			}
			catch(Exception e) {
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
