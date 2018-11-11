package net.imglib2.labkit_rest_api.dvid.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;
import java.util.stream.LongStream;

public class Properties {
	private final List<Value> values;
	private final boolean interporable;
	private final long[] blockSize;
	private final long[] voxelSize;
	private final List<Unit> voxelUnits;
	private final long[] minPoint;
	private final long[] maxPoint;
	private final long[] minIndex;
	private final long[] maxIndex;
	private final int backgroundLabel;


	public Properties(List<Value> values, boolean interporable, long[] blockSize, long[] voxelSize, List<Unit> voxelUnits, long[] minPoint, long[] maxPoint, long[] minIndex, long[] maxIndex, int backgroundLabel) {
		this.values = values;
		this.interporable = interporable;
		this.blockSize = blockSize;
		this.voxelSize = voxelSize;
		this.voxelUnits = voxelUnits;
		this.minPoint = minPoint;
		this.maxPoint = maxPoint;
		this.minIndex = minIndex;
		this.maxIndex = maxIndex;
		this.backgroundLabel = backgroundLabel;
	}

	public static Properties create(String dataType, long[] size) {
		final long[] blockSize = {32, 32, 32};
		return  new Properties(
				Arrays.asList(new Value(dataType, "uint8")),
				true,
				blockSize,
				new long[] {1, 1, 1},
				Arrays.asList(Unit.PIXEL, Unit.PIXEL, Unit.PIXEL),
				new long[] {0, 0, 0},
				initMaxPoint(size),
				new long[] {0, 0, 0},
				initMaxIndex(size, blockSize),
				0
				);
	}

	@JsonProperty("Values")
	public List<Value> getValues() {
		return values;
	}

	@JsonProperty("Interporable")
	public boolean isInterporable() {
		return interporable;
	}

	@JsonProperty("BlockSize")
	public long[] getBlockSize() {
		return blockSize;
	}

	@JsonProperty("VoxelSize")
	public long[] getVoxelSize() {
		return voxelSize;
	}

	@JsonProperty("VoxelUnits")
	public List<Unit> getVoxelUnits() {
		return voxelUnits;
	}

	@JsonProperty("MinPoint")
	public long[] getMinPoint() {
		return minPoint;
	}

	@JsonProperty("MaxPoint")
	public long[] getMaxPoint() {
		return maxPoint;
	}

	@JsonProperty("MinIndex")
	public long[] getMinIndex() {
		return minIndex;
	}

	@JsonProperty("MaxIndex")
	public long[] getMaxIndex() {
		return maxIndex;
	}

	@JsonProperty("Background")
	public int getBackgroundLabel() {
		return backgroundLabel;
	}

	private static long[] initMaxPoint(long[] size) {
		return LongStream.of(size).map(x -> x - 1).toArray();
	}

	private static long[] initMaxIndex(long[] size, long[] blockSize) {
		assert blockSize.length == size.length;
		long[] result = new long[size.length];
		for (int i = 0; i < size.length; i++)
			result[i] = (size[i] - 1) / blockSize[i];
		return result;
	}

}

