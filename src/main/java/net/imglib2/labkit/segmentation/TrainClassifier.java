
package net.imglib2.labkit.segmentation;

import net.imglib2.labkit.Extensible;

import net.imglib2.labkit.models.SegmentationModel;
import net.imglib2.labkit.models.SegmenterListModel;
import net.imglib2.labkit.panel.GuiUtils;

public class TrainClassifier {

	SegmentationModel model;

	public <M extends SegmentationModel & SegmenterListModel<?>> TrainClassifier(Extensible extensible, M model) {
		this.model = model;
		extensible.addAction("Train Classifier", "trainClassifier",
			this::trainClassifier, "ctrl shift T");
		extensible.addSegmenterMenuItem("Train Classifier", ((SegmenterListModel) model)::train, GuiUtils.loadIcon("run.png"));
		extensible.addSegmenterMenuItem("Remove Classifier", ((SegmenterListModel) model)::remove, GuiUtils.loadIcon("remove.png"));
	}

	private void trainClassifier() {
		try {
			model.trainSegmenter();
		}
		catch (final Exception e1) {
			System.out.println("Training was interrupted by exception:");
			e1.printStackTrace();
		}
	}

}
