package net.imglib2.labkit_rest_api.dvid.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Value {
	private final PixelType dataType;
	private final PixelType labelType;

	@Deprecated
	public Value() {
		// this constructor should never be used, but is required for JSON deserialization.
		this(null, null);
	}

	public Value(PixelType dataType, PixelType labelType) {
		this.dataType = dataType;
		this.labelType = labelType;
	}

	@JsonProperty("DataType")
	public PixelType getDataType() {
		return dataType;
	}

	@JsonProperty("Label")
	public PixelType getLabelType() {
		return labelType;
	}
}
