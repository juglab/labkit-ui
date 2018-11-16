package net.imglib2.dvid.metadata;

import javax.json.bind.annotation.JsonbProperty;

public enum Unit {
	@JsonbProperty("nanometers")
	NANOMETERS,
	@JsonbProperty("micrometers")
	MICROMETERS,
	@JsonbProperty("pixel")
	PIXEL
}
