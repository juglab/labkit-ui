package net.imglib2.labkit_rest_api.dvid.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Unit {
	@JsonProperty("nanometers")
	NANOMETERS,
	@JsonProperty("micrometers")
	MICROMETERS,
	@JsonProperty("pixel")
	PIXEL
}
