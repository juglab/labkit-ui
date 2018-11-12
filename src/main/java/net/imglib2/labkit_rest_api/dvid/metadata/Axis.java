package net.imglib2.labkit_rest_api.dvid.metadata;

import javax.json.bind.annotation.JsonbProperty;

public class Axis {
    @JsonbProperty("Label")
    private final String label;

    @JsonbProperty("Resolution")
    private final double resolution;

    @JsonbProperty("Unit")
    private final Unit unit;

    @JsonbProperty("Size")
    private final long size;

    @JsonbProperty("Offset")
    private final long offset;

    public Axis(String label, double resolution, Unit unit, long size, long offset) {
        this.label = label;
        this.resolution = resolution;
        this.unit = unit;
        this.size = size;
        this.offset = offset;
    }

    public static Axis create(String label, long size) {
        return new Axis(label, 1.0, Unit.PIXEL, size, 0);
    }

    public String getLabel() {
        return label;
    }

    public double getResolution() {
        return resolution;
    }

    public Unit getUnit() {
        return unit;
    }

    public long getSize() {
        return size;
    }

    public long getOffset() {
        return offset;
    }
}
