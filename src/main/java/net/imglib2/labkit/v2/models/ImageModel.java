
package net.imglib2.labkit.v2.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Labeling;
import org.apache.commons.io.FilenameUtils;

/**
 * Represents an image, overlaid with a labeling.
 */
public class ImageModel {

	@JsonProperty("nick_name")
	private String name;

	@JsonProperty("image_file")
	private String imageFile;

	@JsonProperty("labeling_file")
	private String labelingFile;

	@JsonIgnore
	private String tmpLabelingFile;

	@JsonIgnore
	private InputImage image;

	@JsonIgnore
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

	public void setName(String name) {
		this.name = name;
	}

	public String getImageFile() {
		return imageFile;
	}

	public String getLabelingFile() {
		return labelingFile;
	}

	public void setLabelingFile(String labelingFile) {
		this.labelingFile = labelingFile;
	}

	public InputImage getImage() {
		return image;
	}

	public void setImage(InputImage image) {
		this.image = image;
	}

	public Labeling getLabeling() {
		return labeling;
	}

	public void setLabeling(Labeling labeling) {
		this.labeling = labeling;
	}

}
