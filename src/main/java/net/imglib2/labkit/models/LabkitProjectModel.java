
package net.imglib2.labkit.models;

import net.imglib2.labkit.utils.Notifier;
import org.scijava.Context;

import java.util.List;

public class LabkitProjectModel {

	private final Context context;

	private Holder<LabeledImage> selectedImage;

	private List<LabeledImage> labeledImages;

	private final Notifier changeNotifier = new Notifier();

	public LabkitProjectModel(Context context,
		List<LabeledImage> labeledImages)
	{
		this.context = context;
		this.selectedImage = new DefaultHolder<>(labeledImages.get(0));
		this.labeledImages = labeledImages;
	}

	public List<LabeledImage> labeledImages() {
		return labeledImages;
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
}
