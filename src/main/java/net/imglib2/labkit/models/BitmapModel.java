
package net.imglib2.labkit.models;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.ARGBType;

public class BitmapModel {

	private final LabelingModel model;

	public BitmapModel(LabelingModel model) {
		this.model = model;
	}

	public boolean isValid() {
		return label() != null;
	}

	public Label label() {
		return model.selectedLabel().get();
	}

	public ARGBType color() {
		return label().color();
	}

	public RandomAccessibleInterval<BitType> bitmap() {
		return model.labeling().get().getRegion(label());
	}

	public void fireBitmapChanged() {
		model.dataChangedNotifier().forEach(Runnable::run);
	}

	public AffineTransform3D transformation() {
		return model.labelTransformation();
	}

	public void makeVisible() {
		final Label label = label();
		if (label == null) return;
		if (label.isActive()) return;
		label.setActive(true);
		Holder<Labeling> holder = model.labeling();
		Labeling labeling = holder.get();
		holder.notifier().forEach(r -> r.accept(labeling));
	}
}
