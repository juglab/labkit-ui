/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.ui.segmentation;

import net.imagej.ImgPlus;
import sc.fiji.labkit.ui.Extensible;

import sc.fiji.labkit.ui.MenuBar;
import sc.fiji.labkit.ui.labeling.Labeling;
import sc.fiji.labkit.ui.models.SegmentationItem;
import sc.fiji.labkit.ui.models.SegmenterListModel;
import sc.fiji.labkit.ui.panel.GuiUtils;
import sc.fiji.labkit.ui.segmentation.weka.PixelClassificationPlugin;
import sc.fiji.labkit.ui.utils.progress.SwingProgressWriter;
import net.imglib2.util.Pair;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.CancellationException;

/**
 * Implements the train classifier, and remove classifier menu items.
 */
public class TrainClassifier {

	private final SegmenterListModel model;

	public TrainClassifier(Extensible extensible, SegmenterListModel model) {
		this.model = model;
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU, "Train Classifier",
			1, this::onTrainClicked,
			GuiUtils.loadIcon("run.png"), "ctrl shift T");
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU, "Remove Classifier",
			3, model::remove,
			GuiUtils.loadIcon("remove.png"), null);
	}

	private void onTrainClicked(SegmentationItem item) {
		if (item != null)
			model.selectedSegmenter().set(item);
		trainSelectedSegmenter();
	}

	public void trainSelectedSegmenter() {
		if (model.selectedSegmenter().get() == null)
			selectSomeSegmenter();
		trainSelectedSegmenter(model);
	}

	private void selectSomeSegmenter() {
		List<SegmentationItem> list = model.segmenters().get();
		SegmentationItem segmentationItem = list.isEmpty()
			? addNewSegmenter()
			: list.get(list.size() - 1);
		model.selectedSegmenter().set(segmentationItem);
	}

	private SegmentationItem addNewSegmenter() {
		PixelClassificationPlugin plugin = new PixelClassificationPlugin();
		model.context().inject(plugin);
		return model.addSegmenter(plugin);
	}

	public static void trainSelectedSegmenter(SegmenterListModel model) {
		train(model.trainingData().get(), model.selectedSegmenter().get());
	}

	private static void train(List<Pair<ImgPlus<?>, Labeling>> trainingData, SegmentationItem item) {
		SwingProgressWriter progressWriter = new SwingProgressWriter(null,
			"Training in Progress");
		progressWriter.setVisible(true);
		progressWriter.setProgressBarVisible(false);
		progressWriter.setDetailsVisible(false);
		try {
			item.train(trainingData);
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
