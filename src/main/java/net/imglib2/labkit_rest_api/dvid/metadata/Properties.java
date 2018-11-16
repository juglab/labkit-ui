package net.imglib2.labkit_rest_api.dvid.metadata;

import javax.json.bind.annotation.JsonbProperty;
import java.util.Arrays;
import java.util.List;
import java.util.stream.LongStream;

public class Properties {

	@JsonbProperty("Values")
	private List<Value> values;

	@JsonbProperty("Interporable")
	private boolean interporable;

	@JsonbProperty("BlockSize")
	private long[] blockSize;

	@JsonbProperty("VoxelSize")
	private long[] voxelSize;

	@JsonbProperty("VoxelUnits")
	private List<Unit> voxelUnits;

	@JsonbProperty("MinPoint")
	private long[] minPoint;

	@JsonbProperty("MaxPoint")
	private long[] maxPoint;

	@JsonbProperty("MinIndex")
	private long[] minIndex;

	@JsonbProperty("MaxIndex")
	private long[] maxIndex;

	@JsonbProperty("Background")
	private int backgroundLabel;

	public Properties() {
	}

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

	public static Properties create(PixelType dataType, long[] size) {
		final long[] blockSize = {32, 32, 32};
		return  new Properties(
				Arrays.asList(new Value(dataType, PixelType.UNSIGNED_BYTE)),
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

	public List<Value> getValues() {
		return values;
	}

	public boolean isInterporable() {
		return interporable;
	}

	public long[] getBlockSize() {
		return blockSize;
	}

	public long[] getVoxelSize() {
		return voxelSize;
	}

	public List<Unit> getVoxelUnits() {
		return voxelUnits;
	}

	public long[] getMinPoint() {
		return minPoint;
	}

	public long[] getMaxPoint() {
		return maxPoint;
	}

	public long[] getMinIndex() {
		return minIndex;
	}

	public long[] getMaxIndex() {
		return maxIndex;
	}

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

	public void setValues(List<Value> values) {
		this.values = values;
	}

	public void setInterporable(boolean interporable) {
		this.interporable = interporable;
	}

	public void setBlockSize(long[] blockSize) {
		this.blockSize = blockSize;
	}

	public void setVoxelSize(long[] voxelSize) {
		this.voxelSize = voxelSize;
	}

	public void setVoxelUnits(List<Unit> voxelUnits) {
		this.voxelUnits = voxelUnits;
	}

	public void setMinPoint(long[] minPoint) {
		this.minPoint = minPoint;
	}

	public void setMaxPoint(long[] maxPoint) {
		this.maxPoint = maxPoint;
	}

	public void setMinIndex(long[] minIndex) {
		this.minIndex = minIndex;
	}

	public void setMaxIndex(long[] maxIndex) {
		this.maxIndex = maxIndex;
	}

	public void setBackgroundLabel(int backgroundLabel) {
		this.backgroundLabel = backgroundLabel;
	}
}
