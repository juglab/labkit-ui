
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LabeledImage that = (LabeledImage) o;

		if (!name.equals(that.name)) return false;
		if (!imageFile.equals(that.imageFile)) return false;
		return labelingFile.equals(that.labelingFile);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + imageFile.hashCode();
		result = 31 * result + labelingFile.hashCode();
		return result;
	}
}
