
package net.imglib2.labkit.segmentation;

import net.imglib2.labkit.Extensible;

import net.imglib2.labkit.MenuBar;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmenterListModel;
import net.imglib2.labkit.panel.GuiUtils;
import net.imglib2.labkit.utils.ParallelUtils;

import java.util.function.Consumer;

public class TrainClassifier {

	private final SegmenterListModel<SegmentationItem> model;

	public TrainClassifier(Extensible extensible, SegmenterListModel<SegmentationItem> model) {
		this.model = model;
		extensible.addMenuItem(MenuBar.SEGMENTER_MENU, "Train Classifier", 1,
			ignore -> ((Runnable) this::trainClassifier).run(), null, "ctrl shift T");
		Consumer<SegmentationItem> train = item -> ParallelUtils.runInOtherThread(
			() -> model.train(item));
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU, "Train Classifier",
			1, train, GuiUtils.loadIcon("run.png"), null);
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU, "Remove Classifier",
			3, (Consumer<SegmentationItem>) ((SegmenterListModel) model)::remove,
			GuiUtils.loadIcon("remove.png"), null);
	}

	private void trainClassifier() {
		model.train(model.selectedSegmenter().get());
	}

}
