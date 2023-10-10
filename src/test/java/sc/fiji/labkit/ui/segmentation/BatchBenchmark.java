
package sc.fiji.labkit.ui.segmentation;

import bdv.export.ProgressWriterConsole;
import net.imglib2.util.StopWatch;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.scijava.Context;
import org.scijava.command.CommandService;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;
import sc.fiji.labkit.ui.BatchSegmenter;
import sc.fiji.labkit.ui.plugin.LabkitSegmentImagesInDirectoryPlugin;
import sc.fiji.labkit.ui.segmentation.weka.TrainableSegmentationSegmenter;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.ExecutionException;

public class BatchBenchmark {

	private static final String base = "/home/arzt/tmp/labkit-memory-test/";

	private static final String inputDirectory = base + "input/";

	private static final String segmenterFile = base + "normal.classifier";

	private static final String outputDirectory = base + "output/";

	private static final String fileFilter = "Untitled_1*.tif";

	private static final boolean useGpu = false;

	public static void main(String... args) throws Exception {
		runNewVersion();
	}

	private static void runOldVersion() throws Exception {
		Context context = SingletonContext.getInstance();
		StopWatch sw = StopWatch.createAndStart();
		TrainableSegmentationSegmenter segmenter = new TrainableSegmentationSegmenter(context);
		segmenter.setUseGpu(useGpu);
		segmenter.openModel(segmenterFile);
		BatchSegmenter batch = new BatchSegmenter(segmenter, new ProgressWriterConsole());
		for (File input : listFiles()) {
			System.out.println(input);
			batch.segment(input, new File(outputDirectory, input.getName()));
		}
		System.out.println(sw);
		context.close();
		System.exit(0);
	}

	private static File[] listFiles() {
		File baseFile = new File(inputDirectory);
		File[] files = baseFile.listFiles((FileFilter) new WildcardFileFilter(fileFilter));
		if (files != null)
			return files;
		return new File[0];
	}

	private static void runNewVersion()
		throws InterruptedException, ExecutionException
	{
		Context context = SingletonContext.getInstance();
		CommandService cmd = context.service(CommandService.class);
		System.out.println("start");
		removeImageFromDirectory(outputDirectory);
		StopWatch stopWatch = StopWatch.createAndStart();
		cmd.run(LabkitSegmentImagesInDirectoryPlugin.class, true,
			"input_directory", inputDirectory,
			"file_filter", fileFilter,
			"output_directory", outputDirectory,
			"output_file_suffix", "segmentation.tif",
			"segmenter_file", segmenterFile,
			"use_gpu", useGpu).get();
		System.out.println("stop");
		System.out.println(stopWatch);
		System.exit(0);
	}

	private static void removeImageFromDirectory(String directory) {
		File d = new File(directory);
		FileFilter fileFilter = new WildcardFileFilter("*.tif");
		File[] files = d.listFiles(fileFilter);
		if (files == null)
			return;
		for (File file : files)
			file.delete();
	}
}
