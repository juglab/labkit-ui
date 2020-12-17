
package net.imglib2.labkit.v2.models;

import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Labeling;
import org.apache.commons.io.FilenameUtils;

/**
 * Represents an image, overlaid with a labeling.
 */
public class ImageModel {

	private String name;

	private String imageFile;

	private String labelingFile;

	private String tmpLabelingFile;

	private InputImage image;

	private Labeling labeling;

	public static ImageModel createForImageFile(String imageFile) {
		ImageModel image = new ImageModel();
		image.name = FilenameUtils.getName(imageFile);
		image.imageFile = imageFile;
		image.labelingFile = imageFile + ".labeling";
		return image;
	}

	// Getter & Setter ...

	public String getName() {
		return name;
	}

	public String getImageFile() {
		return imageFile;
	}

	public InputImage getImage() {
		return image;
	}

	public void setImage(InputImage image) {
		this.image = image;
	}
}
