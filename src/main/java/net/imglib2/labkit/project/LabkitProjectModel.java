
package net.imglib2.labkit.project;

import net.imglib2.labkit.models.DefaultHolder;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.utils.Notifier;
import org.scijava.Context;

import java.util.ArrayList;
import java.util.List;

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
