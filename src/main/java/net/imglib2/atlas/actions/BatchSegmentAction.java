package net.imglib2.atlas.actions;

import net.imglib2.atlas.BatchSegmenter;
import net.imglib2.atlas.MainFrame;
import net.imglib2.atlas.classification.Classifier;
import net.imglib2.trainable_segmention.RevampUtils;
import org.scijava.Cancelable;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandInfo;
import org.scijava.module.Module;
import org.scijava.module.ModuleException;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginService;
import org.scijava.ui.behaviour.util.RunnableAction;
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

	private final MainFrame.Extensible extensible;
	private final Classifier classifier;

	public BatchSegmentAction(MainFrame.Extensible extensible, Classifier classifier) {
		this.extensible = extensible;
		this.classifier = classifier;
		extensible.addAction(new RunnableAction("Segment multiple images", this::segmentImages), "");
	}

	private void segmentImages() {
		BatchSegment commandInstance = new BatchSegment();
		commandInstance.setClassifier(classifier);
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

		private Classifier classifier;

		public void setClassifier(Classifier classifier) {
			this.classifier = classifier;
		}

		@Override
		public void run() {
			BatchSegmenter batchSegmenter = new BatchSegmenter(classifier);
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
