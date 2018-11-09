package net.imglib2.labkit_rest_api;

import com.google.common.collect.HashBiMap;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit_rest_api.dvid.ImageId;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class ImageRepository {

	private static final ImageRepository instance = new ImageRepository();

	private final Map<ImageId, RandomAccessibleInterval<?>> idToImg = HashBiMap.create();

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
		final ImageId imageId = new ImageId(uuid, dataName);
		idToImg.put(imageId, image);
		return imageId;
	}

	public RandomAccessibleInterval<?> getImage(ImageId imageId) {
		return idToImg.get(imageId);
	}
}
