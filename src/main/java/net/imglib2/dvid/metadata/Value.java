package net.imglib2.dvid.metadata;

import javax.json.bind.annotation.JsonbProperty;

public class Value {

	@JsonbProperty("DataType")
	private PixelType dataType;

	@JsonbProperty("Label")
	private PixelType labelType;

	public Value() {
	}

	public Value(PixelType dataType, PixelType labelType) {
		this.dataType = dataType;
		this.labelType = labelType;
	}

	public PixelType getDataType() {
		return dataType;
	}

	public PixelType getLabelType() {
		return labelType;
	}

	public void setDataType(PixelType dataType) {
		this.dataType = dataType;
	}

	public void setLabelType(PixelType labelType) {
		this.labelType = labelType;
	}
}
