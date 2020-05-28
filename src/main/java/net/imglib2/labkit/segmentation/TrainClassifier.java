
package net.imglib2.labkit.segmentation;

import net.imagej.ImgPlus;
import net.imglib2.labkit.Extensible;

import net.imglib2.labkit.MenuBar;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmenterListModel;
import net.imglib2.labkit.panel.GuiUtils;
import net.imglib2.labkit.utils.ParallelUtils;
import net.imglib2.labkit.utils.progress.SwingProgressWriter;
import net.imglib2.util.ValuePair;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TrainClassifier {

	private final SegmenterListModel model;

	public TrainClassifier(Extensible extensible, SegmenterListModel model) {
		this.model = model;
		extensible.addMenuItem(MenuBar.SEGMENTER_MENU, "Train Classifier", 1,
			ignore -> trainSelectedSegmenter(), null, "ctrl shift T");
		Consumer<SegmentationItem> train = item -> ParallelUtils.runInOtherThread(
			() -> trainSegmenter(item));
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU, "Train Classifier",
			1, train, GuiUtils.loadIcon("run.png"), null);
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU, "Remove Classifier",
			3, (Consumer<SegmentationItem>) ((SegmenterListModel) model)::remove,
			GuiUtils.loadIcon("remove.png"), null);
	}

	private void trainSelectedSegmenter() {
		trainSegmenter(model.selectedSegmenter().get());
	}

	private void trainSegmenter(SegmentationItem item) {
		model.train(item);
	}

	public static void train(List<ImageLabelingModel> imageLabelingModels, SegmentationItem item) {
		SwingProgressWriter progressWriter = new SwingProgressWriter(null,
			"Training in Progress");
		progressWriter.setVisible(true);
		progressWriter.setProgressBarVisible(false);
		progressWriter.setDetailsVisible(false);
		try {
			Stream<ValuePair<ImgPlus<?>, Labeling>> stream = imageLabelingModels.stream()
				.map(ilm -> new ValuePair<>(ilm.imageForSegmentation(), ilm.labeling().get()));
			item.train(stream.collect(Collectors.toList()));
		}
		catch (CancellationException e) {
			progressWriter.setVisible(false);
			JOptionPane.showMessageDialog(null, e.getMessage(), "Training Cancelled",
				JOptionPane.PLAIN_MESSAGE);
		}
		catch (Throwable e) {
			progressWriter.setVisible(false);
			JOptionPane.showMessageDialog(null, e.toString(), "Training Failed",
				JOptionPane.WARNING_MESSAGE);
			e.printStackTrace();
		}
		finally {
			progressWriter.setVisible(false);
		}
	}

}
