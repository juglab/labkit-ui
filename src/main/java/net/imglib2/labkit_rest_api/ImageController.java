package net.imglib2.labkit_rest_api;


import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit_rest_api.dvid.ImageId;
import net.imglib2.labkit_rest_api.dvid.ImageMetadata;
import net.imglib2.util.Intervals;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
public class ImageController {

	private final ImageRepository imageRepository;

	public ImageController() {
		this.imageRepository = ImageRepository.getInstance();
	}

	@RequestMapping("/node/{uuid}/{dataName}/metadata")
	public ImageMetadata getImageMetadata(@PathVariable(value = "uuid") String uuid, @PathVariable(value = "dataName") String dataName) {
		ImageId imageId = new ImageId(uuid, dataName);
		RandomAccessibleInterval<?> image = imageRepository.getImage(imageId);
		return ImageMetadata.create(Intervals.dimensionsAsLongArray(image), getDataType(image));
	}

	private String getDataType(RandomAccessibleInterval<?> image) {
		return null;
	}

	@RequestMapping("/nodes")
	public Collection<ImageId> getImageIds() {
		return imageRepository.all();
	}


}
