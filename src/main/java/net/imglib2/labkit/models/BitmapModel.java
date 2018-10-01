
package net.imglib2.labkit.models;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.labeling.Label;
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
		return model.colorMapProvider().colorMap().getColor(label().name());
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
}
