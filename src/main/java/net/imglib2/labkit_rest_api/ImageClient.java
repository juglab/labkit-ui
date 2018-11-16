package net.imglib2.labkit_rest_api;

import bdv.util.BdvFunctions;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.copy.ByteBufferImgCopy;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.labkit_rest_api.dvid.ImageId;
import net.imglib2.labkit_rest_api.dvid.metadata.Axis;
import net.imglib2.labkit_rest_api.dvid.metadata.ImageMetadata;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import javax.swing.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.LongStream;

public class ImageClient {

	private final String url;
	private final Interval interval;
	private final Client client = ClientBuilder.newClient();
	private final int[] blockSize;
	private final RealType<?> type;

	public ImageClient(String url) {
		this.url = url;
		final ImageMetadata metadata = client.target(this.url + "/metadata").request(MediaType.APPLICATION_JSON).get(ImageMetadata.class);
		this.interval = initializeInterval(metadata);
		this.blockSize = initializeBlockSize(metadata);
		this.type = initializeType(metadata);
	}

	public static Img<?> asCachedImg(String url) {
		ImageClient client = new ImageClient(url);
		return client.createCachedImg();
	}

	private Interval initializeInterval(ImageMetadata metadata) {
		List<Axis> axes = metadata.getAxes();
		long[] min = axes.stream().mapToLong(Axis::getOffset).toArray();
		long[] max = axes.stream().mapToLong(axis -> axis.getOffset() + axis.getSize() - 1).toArray();
		return new FinalInterval(min, max);
	}

	private int[] initializeBlockSize(ImageMetadata metadata) {
		return LongStream.of(metadata.getProperties().getBlockSize()).mapToInt(x -> (int) x).toArray();
	}

	private RealType<?> initializeType(ImageMetadata metadata) {
		return (RealType<?>) metadata.getProperties().getValues().get(0).getDataType().getType();
	}

	private static String getUrl() {
		Client client = ClientBuilder.newClient();
		ImageId id = client.target("http://localhost:8572/nodes/").request().get(ImageId[].class)[0];
		return "http://localhost:8572/node/" + id.getUuid() + "/" + id.getDataName();
	}

	private RandomAccessibleInterval<?> getChunk(Interval interval) {
		final byte[] data = getBinaryData(interval);
		final long[] size = Intervals.dimensionsAsLongArray(interval);
		final long[] min = Intervals.minAsLongArray(interval);
		Img<UnsignedByteType> img = new ArrayImgFactory<>((NativeType) type).create(size);
		ByteBufferImgCopy.fromByteBuffer(ByteBuffer.wrap(data), img);
		return Views.translate(img, min);
	}

	public void copyChunk(RandomAccessibleInterval<?> output) {
		ensureType(output);
		ByteBufferImgCopy.fromByteBuffer(ByteBuffer.wrap(getBinaryData(output)), output);
	}

	public void ensureType(RandomAccessibleInterval<?> output) {
		final Object outputType = Util.getTypeFromInterval(output);
		boolean typeMatches = type.getClass().isInstance(outputType);
		if (!typeMatches)
			throw new IllegalArgumentException("The give image has the wrong type. " +
					"Type of the server side image is: " + type.getClass() +
					" type of output image: " + type.getClass());
	}

	private byte[] getBinaryData(Interval interval) {
		final long[] size = Intervals.dimensionsAsLongArray(interval);
		final long[] min = Intervals.minAsLongArray(interval);
		StringBuilder url = new StringBuilder().append(this.url)
				.append("/raw/0_1_2/").append(toString(size)).append("/").append(toString(min)).append("/octet-stream");
		return client.target(url.toString()).request().get(byte[].class);
	}

	private static String toString(long[] longs) {
		StringJoiner joiner = new StringJoiner("_");
		for(long value : longs)
			joiner.add(Long.toString(value));
		return joiner.toString();
	}

	public Img<?> createCachedImg() {
		CellLoader loader = cell -> {
			try {
				copyChunk(cell);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		DiskCachedCellImgOptions options = DiskCachedCellImgOptions.options().cellDimensions(blockSize);
		return new DiskCachedCellImgFactory<>((NativeType) type, options).create(Intervals.dimensionsAsLongArray(interval), loader);
	}

	public static void main(String... args) {
		JFrame frame = new JFrame();
		try {
			DummyApplication.main(args);
			String path = getUrl();
			//String path = "http://localhost:8000/api/node/a9/hello";
			BdvFunctions.show(ImageClient.asCachedImg(path), "image");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
