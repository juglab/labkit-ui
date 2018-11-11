package net.imglib2.labkit_rest_api.dvid.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Value {
	private final String dataType;
	private final String labelType;

	public Value(String dataType, String labelType) {
		this.dataType = dataType;
		this.labelType = labelType;
	}

	@JsonProperty("DataType")
	public String getDataType() {
		return dataType;
	}

	@JsonProperty("Label")
	public String getLabelType() {
		return labelType;
	}
}
