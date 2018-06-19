
package net.imglib2.sparse;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.sparse.NtreeImgFactory;
import net.imglib2.sparse.SparseRandomAccessIntType;
import net.imglib2.sparse.StopWatch;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;

public class Complexity {

	public static void main(String... args) {
		for (long size = 10; size < 400; size += 10) {
			long[] dim = { size, size, size };
			long sparse = benchmarkSparse(dim);
			long ntree = benchmarkNtree(dim);
			System.out.println("size " + size + ", sparse " + sparse + " ms" +
				", ntree " + ntree + " ms");
		}
	}

	private static long benchmarkNtree(long[] dim) {
		return benchmark(new NtreeImgFactory<>(new IntType()).create(dim));
	}

	private static long benchmarkSparse(long[] dim) {
		return benchmark(new SparseRandomAccessIntType(new FinalInterval(dim)));
	}

	private static long benchmark(RandomAccessibleInterval<IntType> sparse) {
		StopWatch sw = new StopWatch();
		for (IntType value : Views.iterable(sparse))
			value.setOne();
		long milliSeconds = sw.timeInMilliSeconds();
		return milliSeconds;
	}
}
