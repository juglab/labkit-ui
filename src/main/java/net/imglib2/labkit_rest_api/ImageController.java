package net.imglib2.labkit_rest_api;


import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.labkit_rest_api.dvid.ImageId;
import net.imglib2.labkit_rest_api.dvid.ImageRepresentation;
import net.imglib2.labkit_rest_api.dvid.metadata.ImageMetadata;
import net.imglib2.util.Intervals;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@RestController
public class ImageController {

	private final ImageRepository imageRepository;

	public ImageController() {
		this.imageRepository = ImageRepository.getInstance();
	}

	@RequestMapping("/node/{uuid}/{dataName}/metadata")
	public ImageMetadata getImageMetadata(@PathVariable(value = "uuid") String uuid, @PathVariable(value = "dataName") String dataName) {
		ImageRepresentation image = getImage(uuid, dataName);
		return ImageMetadata.create(Intervals.dimensionsAsLongArray(image.interval()), image.typeSpecification());
	}

	@RequestMapping(
			value = "/node/{uuid}/{dataName}/raw/{dims}/{size}/{offset}/octet-stream",
			produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
	)
	public byte[] getBinaryData(
			@PathVariable(value = "uuid") String uuid,
			@PathVariable(value = "dataName") String dataName,
			@PathVariable(value = "dims") String dimsAsString,
			@PathVariable(value = "size") String sizeAsString,
			@PathVariable(value = "offset") String offsetAsString
	) {
		ImageRepresentation image = getImage(uuid, dataName);
		long[] dims = parseLongArray(dimsAsString);
		long[] size = parseLongArray(sizeAsString);
		long[] offset = parseLongArray(offsetAsString);
		if(!(Arrays.equals(dims, new long[]{0, 1, 2}) && size.length == 3 & offset.length == 3 ))
			throw new IllegalArgumentException("Only 3d is supported for now");
		long[] max = IntStream.range(0, 3).mapToLong(i -> offset[i] + size[i] - 1).toArray();
		Interval interval = new FinalInterval(offset, max);
		return image.getBinaryData(interval);
	}

	private ImageRepresentation getImage(@PathVariable(value = "uuid") String uuid, @PathVariable(value = "dataName") String dataName) {
		ImageId imageId = new ImageId(uuid, dataName);
		return imageRepository.getDvidImage(imageId);
	}

	static long[] parseLongArray(String value) {
		return Stream.of(value.split("_")).mapToLong(Long::valueOf).toArray();
	}

	@RequestMapping("/nodes")
	public Collection<ImageId> getImageIds() {
		return imageRepository.all();
	}
}
