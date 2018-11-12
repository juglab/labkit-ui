package net.imglib2.labkit_rest_api.dvid.metadata;

import javax.json.bind.annotation.JsonbProperty;
import java.util.Arrays;
import java.util.List;

public class ImageMetadata {
    @JsonbProperty("Axes")
    private final List<Axis> axes;

    @JsonbProperty("Properties")
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

    public List<Axis> getAxes() {
        return axes;
    }

    public Properties getProperties() {
        return properties;
    }
}
