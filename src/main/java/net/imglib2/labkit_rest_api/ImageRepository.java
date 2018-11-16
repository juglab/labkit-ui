package net.imglib2.labkit_rest_api;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit_rest_api.dvid.ImageId;
import net.imglib2.labkit_rest_api.dvid.ImageRepresentation;
import net.imglib2.labkit_rest_api.dvid.ImageWrapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ImageRepository {

	private static final ImageRepository instance = new ImageRepository();

	private final Map<ImageId, ImageRepresentation> idToImg = new HashMap<>();

	private String url;

	private ImageRepository() {
		// private, because this is a singleton
	}

	public static ImageRepository getInstance() {
		return instance;
	}

	public Collection<ImageId> all() {
		return idToImg.keySet();
	}

	public ImageId addImage(String dataName, RandomAccessibleInterval<?> image) {
		String uuid = UUID.randomUUID().toString().substring(0, 5);
		final ImageId id = new ImageId(uuid, dataName);
		idToImg.put(id, ImageWrapper.create(image));
		return id;
	}

	public ImageRepresentation getDvidImage(ImageId id) {
		return idToImg.get(id);
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}
}
