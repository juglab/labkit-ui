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

package sc.fiji.labkit.ui.plugin;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImgPlus;
import net.imglib2.parallel.TaskExecutor;
import net.imglib2.parallel.TaskExecutors;
import net.imglib2.type.Type;
import net.imglib2.util.Cast;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.scijava.Cancelable;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.log.Logger;
import org.scijava.plugin.Parameter;
import sc.fiji.labkit.pixel_classification.gpu.api.GpuPool;
import sc.fiji.labkit.ui.segmentation.SegmentationTool;
import sc.fiji.labkit.ui.utils.progress.StatusServiceProgressWriter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

abstract class AbstractProcessFilesInDirectoryPlugin implements Command, Cancelable {

	@Parameter
	private DatasetIOService io;

	@Parameter
	private DatasetService datasetService;

	@Parameter
	private StatusService statusService;

	@Parameter
	private Logger logger;

	@Parameter(style = "directory")
	private File input_directory;

	@Parameter
	private String file_filter = "*.tif";

	@Parameter(style = "directory")
	private File output_directory;

	@Parameter
	private String output_file_suffix = defaultOutputFileSuffix();

	@Parameter(style = "open")
	private File segmenter_file;

	@Parameter
	private Boolean use_gpu = false;

	@Override
	public void run() {
		SegmentationTool segmenter = new SegmentationTool();
		segmenter.setUseGpu(use_gpu);
		segmenter.openModel(segmenter_file.getAbsolutePath());
		segmenter.setProgressWriter(new StatusServiceProgressWriter(statusService));
		FileFilter wildcardFileFilter = new WildcardFileFilter(file_filter);
		File[] files = input_directory.listFiles(wildcardFileFilter);
		AtomicInteger counter = new AtomicInteger();
		TaskExecutor taskExecutor = getTaskExecutor();
		taskExecutor.forEach(Arrays.asList(files), file -> {
			try {
				processFile(segmenter, files, counter.getAndIncrement());
			}
			catch (Exception e) {
				logger.error(e);
			}
		});
	}

	private TaskExecutor getTaskExecutor() {
		if (use_gpu) {
			TaskExecutor blocksTaskExecutor = fixedNumThreadsTaskExecuter(GpuPool.size(),
				TaskExecutors::multiThreaded);
			TaskExecutor imagesTaskExecutor = fixedNumThreadsTaskExecuter(2, () -> blocksTaskExecutor);
			return imagesTaskExecutor;
		}
		else {
			return fixedNumThreadsTaskExecuter(1, TaskExecutors::multiThreaded);
//			return TaskExecutors.nestedFixedThreadPool( 2, (Runtime.getRuntime().availableProcessors() + 1)/ 2 );
		}
	}

	private TaskExecutor fixedNumThreadsTaskExecuter(int numThreads,
		Supplier<TaskExecutor> multiThreaded)
	{
		ThreadFactory threadFactory = TaskExecutors.threadFactory(multiThreaded);
		ExecutorService executorService = Executors.newFixedThreadPool(numThreads, threadFactory);
		TaskExecutor taskExecutor = TaskExecutors.forExecutorService(executorService);
		return taskExecutor;
	}

	private <T extends Type<T>> void processFile(SegmentationTool segmenter, File[] files, int i)
		throws IOException
	{
		File inputFile = files[i];
		String inputFileName = inputFile.getName();
		String outputFileName = FilenameUtils.getBaseName(inputFileName) + output_file_suffix;
		File outputFile = new File(output_directory, outputFileName);
		if (outputFile.exists()) {
			logger.warn("Labkit: Skipping " + inputFile + " because output file exists already: " +
				outputFile);
			return;
		}
		ImgPlus<?> inputImage = io.open(inputFile.getAbsolutePath()).getImgPlus();
		statusService.showStatus("Labkit " + getDescription() + " " + (i + 1) + "/" + files.length +
			": " + inputFileName);
		ImgPlus<T> restult = Cast.unchecked(processImage(segmenter, inputImage));
		Dataset dataset = datasetService.create(restult);
		io.save(dataset, outputFile.getAbsolutePath());
	}

	protected abstract ImgPlus<?> processImage(SegmentationTool segmenter, ImgPlus<?> inputImage);

	protected abstract String getDescription();

	protected abstract String defaultOutputFileSuffix();

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
