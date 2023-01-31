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

package sc.fiji.labkit.ui.utils;

import bdv.export.ProgressWriter;

import java.util.AbstractList;
import java.util.concurrent.atomic.AtomicBoolean;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.parallel.Parallelization;
import net.imglib2.parallel.TaskExecutor;
import net.imglib2.util.Cast;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Matthias Arzt
 */
public class ParallelUtils {

	/**
	 * Divides the given image into cells of size cellDimensions and executes the
	 * given the given operation on each chunk. Imglib2 {@link Parallelization} is
	 * used to parallelize this process. The {@link ProgressWriter} can be used to
	 * visualize the progress or cancel the operation.
	 */
	public static <T> void applyOperationOnCells(RandomAccessibleInterval<T> image,
		int[] cellDimensions,
		Consumer<RandomAccessibleInterval<T>> operation, ProgressWriter progressWriter)
	{
		List<Interval> cells = getCells(new CellGrid(Intervals.dimensionsAsLongArray(image),
			cellDimensions));
		AtomicInteger numStarted = new AtomicInteger(0);
		AtomicInteger numFinished = new AtomicInteger(0);
		int n = cells.size();
		Parallelization.getTaskExecutor().forEach(cells, cell -> {
			AtomicBoolean cancelled = new AtomicBoolean(false);
			if (cancelled.get()) throw new CancellationException();
			try {
				progressWriter.out().println("Chunk " + numStarted.incrementAndGet() + " of " + n);
				operation.accept(Views.interval(image, cell));
				progressWriter.setProgress((double) numFinished.incrementAndGet() / n);
			}
			catch (CancellationException e) {
				cancelled.set(true);
				throw e;
			}
		});
		progressWriter.setProgress(1.0);
	}

	private static List<Interval> getCells(CellGrid cellGrid) {
		long numCells = Intervals.numElements(cellGrid.getGridDimensions());
		return new AbstractList<Interval>() {

			@Override
			public Interval get(int i) {
				long[] min = new long[cellGrid.numDimensions()];
				int[] dim = new int[cellGrid.numDimensions()];
				cellGrid.getCellDimensions(i, min, dim);
				long[] max = IntStream.range(0, cellGrid.numDimensions()).mapToLong(
					i1 -> min[i1] + dim[i1] - 1).toArray();
				return new FinalInterval(min, max);
			}

			@Override
			public int size() {
				return (int) numCells;
			}
		};
	}

	public static void runInOtherThread(Runnable action) {
		ExecutorService executer = Executors.newSingleThreadExecutor();
		executer.submit(() -> {
			action.run();
			executer.shutdown();
		});
	}

	public static void populateCachedImg(
		RandomAccessibleInterval<?> img, ProgressWriter progressWriter)
	{
		if (img instanceof CachedCellImg) {
			Parallelization.runWithNumThreads(Runtime.getRuntime().availableProcessors(), () -> {
				internPopulateCachedImg(Cast.unchecked(img), progressWriter);
			});
		}
	}

	private static void internPopulateCachedImg(
		CachedCellImg<?, ?> img, ProgressWriter progressWriter)
	{
		final Img<?> cells = img.getCells();
		final long numCells = cells.size();
		final AtomicLong chunk = new AtomicLong();
		forEachPixel(cells, cell -> {
			final long c = chunk.incrementAndGet();
			progressWriter.out().println("Chunk " + c + " of " + numCells);
			progressWriter.setProgress((double) c / numCells);
		});
		progressWriter.setProgress(1.0);
	}

	private static <T> void forEachPixel(Img<T> img, Consumer<T> action) {
		final TaskExecutor te = Parallelization.getTaskExecutor();
		final int numThreads = te.getParallelism();
		final long size = img.size();
		final AtomicLong nextIndex = new AtomicLong();
		final AtomicBoolean cancelled = new AtomicBoolean(false);
		te.forEach(IntStream.range(0, numThreads).boxed().collect(Collectors.toList()), workerIndex -> {
			final Cursor<T> cursor = img.cursor();
			long iCursor = -1;
			for (long i = nextIndex.getAndIncrement(); i < size && !cancelled.get(); i = nextIndex
				.getAndIncrement())
			{
				if (Thread.interrupted()) {
					cancelled.set(true);
				}
				try {
					cursor.jumpFwd(i - iCursor);
					action.accept(cursor.get());
					iCursor = i;
				}
				catch (Exception e) {
					cancelled.set(true);
					throw e;
				}
			}
		});
	}
}
