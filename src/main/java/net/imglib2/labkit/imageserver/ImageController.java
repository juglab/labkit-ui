package net.imglib2.labkit.imageserver;


import net.imglib2.labkit.imageserver.dvid.ImageId;
import net.imglib2.labkit.imageserver.dvid.ImageMetadata;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController
public class ImageController {

	private final ImageRepository imageRepository;

	public ImageController(ImageRepository imageRepository) {
		this.imageRepository = imageRepository;
	}

	@RequestMapping("/node/{uuid}/{dataName}/metadata")
	public ImageMetadata greeting(@PathVariable(value="uuid") String uuid, @PathVariable(value="dataName") String dataName) {
		//TODO: retrieve the metadata by UUID
		return ImageMetadata.create(new long[]{100, 90, 50} , "uint8");
	}

	@RequestMapping("/nodes")
	public Collection<ImageId> getImageIds() {
		return imageRepository.all();
	}


}
