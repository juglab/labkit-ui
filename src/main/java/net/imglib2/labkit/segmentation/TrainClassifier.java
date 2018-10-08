
package net.imglib2.labkit.segmentation;

import net.imglib2.labkit.Extensible;

import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmentationModel;
import net.imglib2.labkit.models.SegmenterListModel;
import net.imglib2.labkit.panel.GuiUtils;

import java.util.function.Consumer;

public class TrainClassifier {

	SegmentationModel model;

	public <M extends SegmentationModel & SegmenterListModel<?>> TrainClassifier(Extensible extensible, M model) {
		this.model = model;
		extensible.addAction("Train Classifier", "trainClassifier",
			this::trainClassifier, "ctrl shift T");
		extensible.addMenuItem( SegmentationItem.SEGMENTER_MENU, "Train Classifier",
				(Consumer< SegmentationItem >) ((SegmenterListModel) model)::train,
				GuiUtils.loadIcon("run.png"));
		extensible.addMenuItem( SegmentationItem.SEGMENTER_MENU, "Remove Classifier",
				(Consumer< SegmentationItem >) ((SegmenterListModel) model)::remove,
				GuiUtils.loadIcon("remove.png"));
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
