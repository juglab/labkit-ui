
package net.imglib2.labkit.utils;

import bdv.export.ProgressWriter;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BoolType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * @author Matthias Arzt
 */
public class ParallelUtils {

	public static <T> List<Callable<Void>> chunkOperation(Img<T> image,
		int[] cellDimensions, Consumer<RandomAccessibleInterval<T>> operation)
	{
		return getCells(new CellGrid(Intervals.dimensionsAsLongArray(image),
			cellDimensions)).map(interval -> (Callable<Void>) (() -> {
				operation.accept(Views.interval(image, interval));
				return null;
			})).collect(Collectors.toList());
	}

	private static Stream<Interval> getCells(CellGrid cellGrid) {
		long numCells = LongStream.of(cellGrid.getGridDimensions()).reduce(1, (a,
			b) -> a * b);
		return LongStream.range(0, numCells).mapToObj(i -> getCellOfIndex(cellGrid,
			i));
	}

	private static Interval getCellOfIndex(CellGrid cellGrid, long index) {
		long[] min = new long[cellGrid.numDimensions()];
		int[] dim = new int[cellGrid.numDimensions()];
		cellGrid.getCellDimensions(index, min, dim);
		long[] max = IntStream.range(0, cellGrid.numDimensions()).mapToLong(
			i -> min[i] + dim[i] - 1).toArray();
		return new FinalInterval(min, max);
	}

	public static List<Callable<Void>> addProgress(List<Callable<Void>> chunks,
		ProgressWriter progressWriter)
	{
		AtomicInteger i = new AtomicInteger(0);
		int n = chunks.size();
		BooleanType cancelled = new BoolType(false);
		return chunks.stream().map(runnable -> (Callable<Void>) (() -> {
			if (cancelled.get()) throw new CancellationException();
			try {
				progressWriter.out().println("Chunk " + i + " of " + n);
				runnable.call();
				progressWriter.setProgress((double) i.incrementAndGet() / n);
			}
			catch (CancellationException e) {
				cancelled.set(true);
				throw e;
			}
			return null;
		})).collect(Collectors.toList());
	}

	public static void executeInParallel(ExecutorService executor,
		List<Callable<Void>> collection)
	{
		CheckedExceptionUtils.run(() -> {
			for (Future<Void> future : executor.invokeAll(collection)) {
				future.get();
			}
		});
	}

	public static void runInOtherThread(Runnable action) {
		ExecutorService executer = Executors.newSingleThreadExecutor();
		executer.submit(() -> {
			action.run();
			executer.shutdown();
		});
	}
}
