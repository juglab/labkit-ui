package net.imglib2.dvid.metadata;

import javax.json.bind.annotation.JsonbProperty;
import java.util.Arrays;
import java.util.List;

public class ImageMetadata {
	@JsonbProperty("Axes")
	private List<Axis> axes;

	@JsonbProperty("Properties")
	private Properties properties;

	public ImageMetadata() {
	}

	public static ImageMetadata create(long[] size, PixelType dataType) {
		assert size.length == 3; // TODO: implement for other number of dimensions.
		final ImageMetadata metadata = new ImageMetadata();
		metadata.setAxes(Arrays.asList(Axis.create("X", size[0]), Axis.create("Y", size[1]), Axis.create("Z", size[2])));
		metadata.setProperties(Properties.create(dataType, size));
		return metadata;
	}

	public List<Axis> getAxes() {
		return axes;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setAxes(List<Axis> axes) {
		this.axes = axes;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}
}
