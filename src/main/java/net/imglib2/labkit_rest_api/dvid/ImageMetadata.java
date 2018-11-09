package net.imglib2.labkit_rest_api.dvid;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

public class ImageMetadata {
	private final List<Axis> axes;
	private final Properties properties;

	public ImageMetadata(List<Axis> axes, Properties properties) {
		this.axes = axes;
		this.properties = properties;
	}

	public static ImageMetadata create(long[] size, String dataType) {
		assert size.length == 3; // TODO: implement for other number of dimensions.
		return new ImageMetadata(
				Arrays.asList(Axis.create("X", size[0]), Axis.create("Y", size[1]), Axis.create("Z", size[2])),
				Properties.create(dataType, size)
		);
	}

	@JsonProperty("Axes")
	public List<Axis> getAxes() {
		return axes;
	}

	@JsonProperty("Properties")
	public Properties getProperties() {
		return properties;
	}
}
