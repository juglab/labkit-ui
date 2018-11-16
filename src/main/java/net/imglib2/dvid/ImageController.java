package net.imglib2.dvid;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.dvid.metadata.ImageMetadata;
import net.imglib2.util.Intervals;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class ImageController {
	private final ImageRepository imageRepository = ImageRepository.getInstance();

	@GET
	@Path("node/{uuid}/{dataName}/metadata")
	public ImageMetadata getImageMetadata(
			@PathParam("uuid") String uuid,
			@PathParam("dataName") String dataName) {
		ImageRepresentation image = getImage(uuid, dataName);
		return ImageMetadata.create(Intervals.dimensionsAsLongArray(image.interval()), image.typeSpecification());
	}

	@GET
	@Path("node/{uuid}/{dataName}/raw/{dims}/{size}/{offset}/octet-stream")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] getBinaryData(
			@PathParam("uuid") String uuid,
			@PathParam("dataName") String dataName,
			@PathParam("dims") String dimsAsString,
			@PathParam("size") String sizeAsString,
			@PathParam("offset") String offsetAsString) {
		try {
			ImageRepresentation image = getImage(uuid, dataName);
			long[] dims = parseLongArray(dimsAsString);
			long[] size = parseLongArray(sizeAsString);
			long[] offset = parseLongArray(offsetAsString);
			if (!(Arrays.equals(dims, new long[]{0, 1, 2}) && size.length == 3 & offset.length == 3))
				throw new IllegalArgumentException("Only 3d is supported for now");
			long[] max = IntStream.range(0, 3).mapToLong(i -> offset[i] + size[i] - 1).toArray();
			Interval interval = new FinalInterval(offset, max);
			return image.getBinaryData(interval);
		} catch (Exception e) {
			System.err.println("raw endpoint failed, for " + uuid + "/" + dataName + " error: " + e.getMessage());
			e.printStackTrace();
			return new byte[0];
		}
	}

	private ImageRepresentation getImage(String uuid, String dataName) {
		ImageId imageId = new ImageId(uuid, dataName);
		return imageRepository.getDvidImage(imageId);
	}

	static long[] parseLongArray(String value) {
		return Stream.of(value.split("_")).mapToLong(Long::valueOf).toArray();
	}

	@GET
	@Path("nodes")
	public Collection<ImageId> getImageIds() {
		return imageRepository.all();
	}
}
