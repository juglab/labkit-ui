
package net.imglib2.labkit.models;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imglib2.Interval;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.labeling.LabelingSerializer;
import net.imglib2.labkit.utils.CheckedExceptionUtils;
import org.apache.commons.io.FilenameUtils;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

/**
 * Represents an {@link ImageLabelingModel} that is save to disk.
 */
public class LabeledImage {

	private final Context context;

	private String name;

	private final String imageFile;

	private final String labelingFile;

	private final String modifiedLabelingFile;

	private ImageLabelingModel imageLabelingModel;

	private String storedIn = null;

	private final Runnable onLabelingChanged = this::onLabelingChanged;

	public LabeledImage(Context context, String imageFile) {
		this(context, FilenameUtils.getName(imageFile), imageFile, imageFile + ".labeling");
	}

	public LabeledImage(Context context, String name, String imageFile, String labelingFile) {
		this.context = context;
		this.name = name;
		this.imageFile = imageFile;
		this.labelingFile = labelingFile;
		this.modifiedLabelingFile = initModifiedLabelingFile();
		this.storedIn = labelingFile;
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

	private String initModifiedLabelingFile() {
		File file = new File(labelingFile);
		String name = file.getName();
		File parent = file.getParentFile();
		return new File(parent, "~" + name).getAbsolutePath();
	}

	/**
	 * Opens an {@link ImageLabelingModel}. Changes to the labeling will be tracked.
	 *
	 * @return The opened {@link ImageLabelingModel}.
	 */
	public ImageLabelingModel open() {
		this.imageLabelingModel = snapshot();
		imageLabelingModel.dataChangedNotifier().add(onLabelingChanged);
		imageLabelingModel.labeling().notifier().add(onLabelingChanged);
		return imageLabelingModel;
	}

	/**
	 * Closes the opened {@link ImageLabelingModel}. Changes to the labeling are
	 * saved to a temporary file.
	 */
	public void close() {
		if (imageLabelingModel == null)
			return;
		imageLabelingModel.dataChangedNotifier().remove(onLabelingChanged);
		imageLabelingModel.labeling().notifier().remove(onLabelingChanged);
		saveTemporarily(imageLabelingModel);
		imageLabelingModel = null;
	}

	/**
	 * Discards all changes that where made to the labeling. Deletes the temporary
	 * file. And reloads the labeling.
	 */
	public void discardChanges() {
		if (labelingFile.equals(storedIn))
			return;
		if (imageLabelingModel != null) {
			imageLabelingModel.labeling().set(openOrEmptyLabeling(labelingFile, imageLabelingModel
				.imageForSegmentation().get()));
		}
		try {
			Files.deleteIfExists(Paths.get(modifiedLabelingFile));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		storedIn = labelingFile;
	}

	/**
	 * Writes the labeling (if modified) to {@link #labelingFile}.
	 */
	public void save() {
		if (labelingFile.equals(storedIn))
			return;
		if (imageLabelingModel == null) {
			try {
				Files.move(Paths.get(modifiedLabelingFile), Paths.get(labelingFile),
					StandardCopyOption.REPLACE_EXISTING);
				storedIn = labelingFile;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				new LabelingSerializer(context).save(imageLabelingModel.labeling().get(),
					labelingFile);
				storedIn = labelingFile;
				Files.deleteIfExists(Paths.get(modifiedLabelingFile));
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void onLabelingChanged() {
		storedIn = null;
	}

	private ImageLabelingModel snapshot() {
		if (imageLabelingModel != null)
			return imageLabelingModel;
		DatasetInputImage inputImage = openInputImage();
		inputImage.setDefaultLabelingFilename(modifiedLabelingFile);
		Labeling labeling = openOrEmptyLabeling(storedIn, inputImage.imageForSegmentation());
		ImageLabelingModel imageLabelingModel = new ImageLabelingModel(inputImage);
		imageLabelingModel.labeling().set(labeling);
		return imageLabelingModel;
	}

	private DatasetInputImage openInputImage() {
		DatasetIOService datasetIOService = context.service(DatasetIOService.class);
		Dataset dataset = CheckedExceptionUtils.run(() -> datasetIOService.open(imageFile));
		return new DatasetInputImage(dataset);
	}

	private void saveTemporarily(ImageLabelingModel imageLabelingModel) {
		if (storedIn == null) {
			String filename = modifiedLabelingFile;
			try {
				new LabelingSerializer(context).save(imageLabelingModel.labeling().get(),
					filename);
				storedIn = filename;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private Labeling openOrEmptyLabeling(String filename, Interval interval) {
		if (new File(filename).exists()) {
			try {
				return new LabelingSerializer(context).open(filename);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return Labeling.createEmpty(Arrays.asList("background", "foreground"), interval);
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
