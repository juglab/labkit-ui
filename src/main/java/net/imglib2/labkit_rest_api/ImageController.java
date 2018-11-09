package net.imglib2.labkit_rest_api;


import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit_rest_api.dvid.ImageId;
import net.imglib2.labkit_rest_api.dvid.ImageMetadata;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.plugin.dom.exception.InvalidStateException;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
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
		RandomAccessibleInterval<?> image = getImage(uuid, dataName);
		return ImageMetadata.create(Intervals.dimensionsAsLongArray(image), getDataType(image));
	}

	private RandomAccessibleInterval<?> getImage(@PathVariable(value = "uuid") String uuid, @PathVariable(value = "dataName") String dataName) {
		ImageId imageId = new ImageId(uuid, dataName);
		return imageRepository.getImage(imageId);
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
		RandomAccessibleInterval<?> image = getImage(uuid, dataName);
		long[] dims = parseLongArray(dimsAsString);
		long[] size = parseLongArray(sizeAsString);
		long[] offset = parseLongArray(offsetAsString);
		if(!(Arrays.equals(dims, new long[]{0, 1, 2}) && size.length == 3 & offset.length == 3 ))
			throw new IllegalArgumentException("Only 3d is supported for now");
		long[] max = IntStream.range(0, 3).mapToLong(i -> offset[i] + size[i] - 1).toArray();
		Interval interval = new FinalInterval(offset, max);
		RandomAccessibleInterval<?> chunk = Views.interval(image, interval);
		return toBytes(chunk);
	}

	private <T> byte[] toBytes(RandomAccessibleInterval<T> chunk) {
		T pixelType = Util.getTypeFromInterval(chunk);
		ByteBuffer buffer = ByteBuffer.allocate( (int) Intervals.numElements( bytesPerPixel(pixelType) ) );
		Consumer<T> pixelAdder = pixelAdder(buffer, pixelType);
		for(T pixel : Views.flatIterable(chunk))
			pixelAdder.accept(pixel);
		return buffer.array();
	}

	private <T> Consumer<T> pixelAdder(ByteBuffer buffer, T pixelType) {
		if(pixelType instanceof UnsignedByteType)
			return pixel -> buffer.put(((UnsignedByteType) pixel).getByte());
		if(pixelType instanceof UnsignedShortType)
			return pixel -> buffer.putShort(((UnsignedShortType) pixel).getShort());
	}

	private int bytesPerPixel(Object pixelType) {
		if(pixelType instanceof RealType)
			return ((RealType) pixelType).getBitsPerPixel() / 8;
		throw new InvalidStateException("unsupported pixel type");
	}

	static long[] parseLongArray(String value) {
		return Stream.of(value.split("_")).mapToLong(Long::valueOf).toArray();
	}

	private String getDataType(RandomAccessibleInterval<?> image) {
		return null;
	}

	@RequestMapping("/nodes")
	public Collection<ImageId> getImageIds() {
		return imageRepository.all();
	}

}
