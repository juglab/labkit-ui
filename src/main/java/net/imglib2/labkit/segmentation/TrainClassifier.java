
package net.imglib2.labkit.segmentation;

import net.imglib2.labkit.DefaultExtensible;
import net.imglib2.labkit.Extensible;

import net.imglib2.labkit.MenuBar;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmentationModel;
import net.imglib2.labkit.models.SegmenterListModel;
import net.imglib2.labkit.panel.GuiUtils;

import java.util.function.Consumer;

public class TrainClassifier {

	SegmentationModel model;

	public <M extends SegmentationModel & SegmenterListModel<?>> TrainClassifier(Extensible extensible, M model) {
		this.model = model;
		extensible.addMenuItem( MenuBar.SEGMENTER_MENU, "Train Classifier", 1, ignore -> ((Runnable) this::trainClassifier)
				.run(), null, "ctrl shift T");
		extensible.addMenuItem( SegmentationItem.SEGMENTER_MENU, "Train Classifier", 1,
				(Consumer< SegmentationItem >) ((SegmenterListModel) model)::train,
				GuiUtils.loadIcon("run.png"), null);
		extensible.addMenuItem( SegmentationItem.SEGMENTER_MENU, "Remove Classifier", 3,
				(Consumer< SegmentationItem >) ((SegmenterListModel) model)::remove,
				GuiUtils.loadIcon("remove.png"), null);
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
