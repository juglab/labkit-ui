
package net.imglib2.labkit.models;

import net.imglib2.labkit.utils.Notifier;
import org.scijava.Context;

import java.util.ArrayList;
import java.util.List;

public class LabkitProjectModel {

	private final Context context;

	private final String projectDirectory;

	private Holder<LabeledImage> selectedImage;

	private List<LabeledImage> labeledImages;

	private List<String> segmenterFiles;

	private final Notifier changeNotifier = new Notifier();

	public LabkitProjectModel(Context context,
		String projectDirectory,
		List<LabeledImage> labeledImages)
	{
		this.context = context;
		this.projectDirectory = projectDirectory;
		this.selectedImage = new DefaultHolder<>(labeledImages.size() == 0 ? null : labeledImages.get(
			0));
		this.labeledImages = labeledImages;
		this.segmenterFiles = new ArrayList<>();
		changeNotifier.add(this::onLabeledImagesChanged);
	}

	public String getProjectDirectory() {
		return projectDirectory;
	}

	public List<LabeledImage> labeledImages() {
		return labeledImages;
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
		if (labeledImages.contains(selectedImage.get()))
			return;
		if (!labeledImages.isEmpty())
			selectedImage.set(labeledImages.get(0));
		else
			selectedImage.set(null);
	}
}
