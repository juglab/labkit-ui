
package net.imglib2.sparse;

import net.imglib2.img.sparse.NtreeImgFactory;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.Views;

/**
 * @author Matthias Arzt
 */
@State(Scope.Benchmark)
public class SparseRandomAccessIntTypeBenchmark {

	// RandomAccessibleInterval<BitType> image = new
	// DiskCachedCellImgFactory<BitType>().create(new long[]{100,100, 100}, new
	// BitType());
	// RandomAccessibleInterval<ByteType> image = ArrayImgs.bytes(100,100,100);
	// RandomAccessibleInterval<IntType> image = ArrayImgs.ints(100,100,100);
	// RandomAccessibleInterval<BitType> image = ArrayImgs.bits(100,100,100);
	RandomAccessibleInterval<IntType> ntree = new NtreeImgFactory<>(new IntType())
		.create(new long[] { 100, 100, 100 });
	RandomAccessibleInterval<IntType> sparse = new SparseRandomAccessIntType(
		Intervals.createMinSize(0, 0, 0, 100, 100, 100));

	@Benchmark
	public void fillNtree() {
		for (IntegerType pixel : Views.iterable(ntree))
			pixel.setOne();
	}

	@Benchmark
	public void fillSparse() {
		for (IntegerType pixel : Views.iterable(sparse))
			pixel.setOne();
	}

	public static void main(final String... args) throws RunnerException {
		final Options opt = new OptionsBuilder().include(
			SparseRandomAccessIntTypeBenchmark.class.getSimpleName()).forks(1)
			.warmupIterations(4).measurementIterations(8).warmupTime(TimeValue
				.milliseconds(1000)).measurementTime(TimeValue.milliseconds(1000))
			.build();
		new Runner(opt).run();
	}

}
