
package net.imglib2.labkit.v2.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.imglib2.labkit.InitialLabeling;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.labeling.LabelingSerializer;
import net.imglib2.labkit.v2.utils.InputImageIoUtils;
import org.apache.commons.io.FilenameUtils;
import org.scijava.Context;

import java.io.IOException;

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
	private InputImage image;

	@JsonIgnore
	private Labeling labeling;

	@JsonIgnore
	private boolean labelingModified = false;

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

	public void setImageFile(String imageFile) {
		this.imageFile = imageFile;
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

	public boolean isLabelingModified() {
		return labelingModified;
	}

	public void setLabelingModified(boolean labelingModified) {
		this.labelingModified = labelingModified;
	}

	// Move this into a ImageModelController

	public void load(Context context, String projectFolder) {
		loadImage(context);
		loadLabeling(context, projectFolder);
	}

	private void loadLabeling(Context context, String projectFolder) {
		if (this.getLabeling() == null) {
			String fullLabelingFile = FilenameUtils.concat(
				projectFolder, this
					.getLabelingFile());
			Labeling labeling = InitialLabeling
				.initialLabeling(context, this.getImage(),
					fullLabelingFile);
			setLabeling(labeling);
		}
	}

	private void loadImage(Context context) {
		if (this.getImage() == null)
			setImage(InputImageIoUtils.open(context, getImageFile()));
	}

	public void saveLabeling(String projectFolder, boolean inplace, Context context) {
		Labeling labeling = this.getLabeling();
		if (labeling == null || (inplace && !this.isLabelingModified()))
			return;
		try {
			String labelingFile = FilenameUtils.concat(projectFolder, this.getLabelingFile());
			new LabelingSerializer(context).save(labeling, labelingFile);
			if (inplace)
				this.setLabelingModified(false);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
