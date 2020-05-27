package net.imglib2.labkit.multi_image;

public class ImageItem {

	private String imageFile;

	private String labelingFile;

	public ImageItem(String imageFile)	{
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
		return "image: " + imageFile + " labeling: " + labelingFile;
	}
}
