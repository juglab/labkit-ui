package net.imglib2.labkit.imageserver;

import com.google.common.collect.HashBiMap;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.imageserver.dvid.ImageId;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Repository
public class ImageRepository {
	private final Map<ImageId, RandomAccessibleInterval<?>> idToImg = HashBiMap.create();

	public Collection<ImageId> all() {
		return idToImg.keySet();
	}

	public ImageId addImage(RandomAccessibleInterval<?> image) {
		String uuid = UUID.randomUUID().toString().substring(0, 5);
		final ImageId imageId = new ImageId(uuid, "image");
		idToImg.put(imageId, image);
		return imageId;
	}
}
