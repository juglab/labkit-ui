package net.imglib2.labkit_rest_api.dvid.metadata;

import javax.json.bind.annotation.JsonbProperty;
import java.util.Arrays;
import java.util.List;
import java.util.stream.LongStream;

public class Properties {
    @JsonbProperty("Values")
    private final List<Value> values;

    @JsonbProperty("Interporable")
    private final boolean interporable;

    @JsonbProperty("BlockSize")
    private final long[] blockSize;

    @JsonbProperty("VoxelSize")
    private final long[] voxelSize;

    @JsonbProperty("VoxelUnits")
    private final List<Unit> voxelUnits;

    @JsonbProperty("MinPoint")
    private final long[] minPoint;

    @JsonbProperty("MaxPoint")
    private final long[] maxPoint;

    @JsonbProperty("MinIndex")
    private final long[] minIndex;

    @JsonbProperty("MaxIndex")
    private final long[] maxIndex;

    @JsonbProperty("Background")
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
        return new Properties(
                Arrays.asList(new Value(dataType, "uint8")),
                true,
                blockSize,
                new long[]{1, 1, 1},
                Arrays.asList(Unit.PIXEL, Unit.PIXEL, Unit.PIXEL),
                new long[]{0, 0, 0},
                initMaxPoint(size),
                new long[]{0, 0, 0},
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
}
