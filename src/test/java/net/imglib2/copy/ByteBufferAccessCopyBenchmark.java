
package net.imglib2.copy;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.type.numeric.real.DoubleType;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.nio.ByteBuffer;

/**
 * @author Matthias Arzt
 */
@State(Scope.Benchmark)
public class ByteBufferAccessCopyBenchmark {

	private int size = 250;
	private ByteBuffer buffer = ByteBuffer.allocate(size * 8);
	private DoubleArray access = new DoubleArray(size);

	@Benchmark
	public void hardCoded() {
		buffer.rewind();
		buffer.asDoubleBuffer().get(access.getCurrentStorageArray());
	}

	@Benchmark
	public void fromByteBuffer() {
		buffer.rewind();
		ByteBufferAccessCopy.fromByteBuffer(buffer, access);
	}

	public static void main(final String... args) throws RunnerException {
		final Options opt = new OptionsBuilder().include(
			ByteBufferAccessCopyBenchmark.class.getSimpleName()).forks(0)
			.warmupIterations(10).measurementIterations(8).warmupTime(TimeValue
				.milliseconds(100)).measurementTime(TimeValue.milliseconds(100))
			.build();
		new Runner(opt).run();
	}

}
