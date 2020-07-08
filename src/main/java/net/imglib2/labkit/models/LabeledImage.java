
package net.imglib2.labkit.models;

import java.io.File;

public class LabeledImage {

	private String imageFile;

	private String labelingFile;

	public LabeledImage(String imageFile) {
		this.imageFile = imageFile;
		this.labelingFile = imageFile + ".labeling";
	}

	public String getImageFile() {
		return imageFile;
	}

	public String getLabelingFile() {
		return labelingFile;
	}

	@Override
	public String toString() {
		return new File(imageFile).getName();
	}
}
