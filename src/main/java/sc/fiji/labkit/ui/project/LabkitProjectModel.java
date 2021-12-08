
package sc.fiji.labkit.ui.project;

import sc.fiji.labkit.ui.models.DefaultHolder;
import sc.fiji.labkit.ui.models.Holder;
import sc.fiji.labkit.ui.utils.Notifier;
import org.scijava.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Labkit project, that contains a list of labeled images and
 * trained segmentation algorithms.
 */
public class LabkitProjectModel {

	private final Context context;

	private final String projectDirectory;

	private Holder<LabeledImage> selectedImage;

	private List<LabeledImage> labeledImageFiles;

	private List<String> segmenterFiles;

	private final Notifier changeNotifier = new Notifier();

	public LabkitProjectModel(Context context,
		String projectDirectory,
		List<LabeledImage> labeledImageFiles)
	{
		this.context = context;
		this.projectDirectory = projectDirectory;
		this.selectedImage = new DefaultHolder<>(labeledImageFiles.size() == 0 ? null
			: labeledImageFiles.get(
				0));
		this.labeledImageFiles = labeledImageFiles;
		this.segmenterFiles = new ArrayList<>();
		changeNotifier.addListener(this::onLabeledImagesChanged);
	}

	public String getProjectDirectory() {
		return projectDirectory;
	}

	public List<LabeledImage> labeledImages() {
		return labeledImageFiles;
	}

	public List<String> segmenterFiles() {
		return segmenterFiles;
	}

	public Context context() {
		return context;
	}

	public Notifier changeNotifier() {
		return changeNotifier;
	}

	public Holder<LabeledImage> selectedImage() {
		return selectedImage;
	}

	private void onLabeledImagesChanged() {
		if (labeledImageFiles.contains(selectedImage.get()))
			return;
		if (!labeledImageFiles.isEmpty())
			selectedImage.set(labeledImageFiles.get(0));
		else
			selectedImage.set(null);
	}
}
