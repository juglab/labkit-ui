
package net.imglib2.labkit.models;

import org.apache.commons.io.FilenameUtils;

public class LabeledImage {

	private String name;

	private final String imageFile;

	private final String labelingFile;

	public LabeledImage(String imageFile) {
		this(FilenameUtils.getName(imageFile), imageFile, imageFile + ".labeling");
	}

	public LabeledImage(String name, String imageFile, String labelingFile) {
		this.name = name;
		this.imageFile = imageFile;
		this.labelingFile = labelingFile;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getImageFile() {
		return imageFile;
	}

	public String getLabelingFile() {
		return labelingFile;
	}

	@Override
	public String toString() {
		return name;
	}
}
