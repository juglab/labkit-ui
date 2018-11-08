package net.imglib2.labkit.imageserver.dvid;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Unit {
	@JsonProperty("nanometers")
	NANOMETERS,
	@JsonProperty("micrometers")
	MICROMETERS,
	@JsonProperty("pixel")
	PIXEL
}
