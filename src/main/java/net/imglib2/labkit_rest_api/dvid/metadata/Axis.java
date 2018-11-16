package net.imglib2.labkit_rest_api.dvid.metadata;

import javax.json.bind.annotation.JsonbProperty;

public class Axis {

	@JsonbProperty("Label")
	private String label;

	@JsonbProperty("Resolution")
	private double resolution;

	@JsonbProperty("Unit")
	private Unit unit;

	@JsonbProperty("Size")
	private long size;

	@JsonbProperty("Offset")
	private long offset;

	public Axis() {
	}

	public static Axis create(String label, long size) {
		Axis axis = new Axis();
		axis.setLabel(label);
		axis.setResolution(1);
		axis.setSize(size);
		axis.setOffset(0);
		return axis;
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

	public void setLabel(String label) {
		this.label = label;
	}

	public void setResolution(double resolution) {
		this.resolution = resolution;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}
}
