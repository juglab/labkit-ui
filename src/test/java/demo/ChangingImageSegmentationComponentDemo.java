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

package demo;

import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imagej.patcher.LegacyInjector;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.VirtualStackAdapter;
import net.imglib2.img.display.imagej.ImageJFunctions;
import sc.fiji.labkit.ui.InitialLabeling;
import sc.fiji.labkit.ui.SegmentationComponent;
import sc.fiji.labkit.ui.inputimage.DatasetInputImage;
import sc.fiji.labkit.ui.models.DefaultSegmentationModel;
import sc.fiji.labkit.ui.models.ImageLabelingModel;
import sc.fiji.labkit.ui.models.SegmentationModel;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.awt.*;

public class ChangingImageSegmentationComponentDemo {

	static {
		LegacyInjector.preinit();
	}

	private final SegmentationComponent segmenter;

	private final DefaultSegmentationModel segmentationModel;

	public static void main(String... args) {
		new ChangingImageSegmentationComponentDemo();
	}

	private ChangingImageSegmentationComponentDemo() {
		JFrame frame = setupFrame();
		ImgPlus<?> image = VirtualStackAdapter.wrap(new ImagePlus(
			"https://imagej.nih.gov/ij/images/FluorescentCells.jpg"));
		Context context = new Context();
		segmentationModel = new DefaultSegmentationModel(context, new DatasetInputImage(image));
		segmenter = new SegmentationComponent(frame, segmentationModel, false);
		frame.add(segmenter);
		frame.add(initChangeImageButton(segmentationModel), BorderLayout.PAGE_START);
		frame.add(getBottomPanel(), BorderLayout.PAGE_END);
		frame.setVisible(true);
	}

	private JButton initChangeImageButton(SegmentationModel segmentationModel) {
		JButton button = new JButton("change image");
		button.addActionListener(ignore -> ChangingImageSegmentationComponentDemo
			.onChangeImageButtonClicked(segmentationModel));
		return button;
	}

	private static void onChangeImageButtonClicked(SegmentationModel segmentationModel) {
		final ImagePlus imp = new ImagePlus(
			"https://imagej.nih.gov/ij/images/apple.tif");
		ImageLabelingModel model = segmentationModel.imageLabelingModel();
		ImgPlus<?> image = VirtualStackAdapter.wrap(imp);
		DatasetInputImage datasetInputImage = new DatasetInputImage(image);
		model.showable().set(datasetInputImage.showable());
		model.imageForSegmentation().set(datasetInputImage.imageForSegmentation());
		model.labeling().set(InitialLabeling.initialLabeling(SingletonContext.getInstance(),
			datasetInputImage));
	}

	private JPanel getBottomPanel() {
		JButton segmentation = new JButton(new RunnableAction("Show Segmentation",
			this::showSegmentation));
		JButton prediction = new JButton(new RunnableAction("Show Prediction",
			this::showPrediction));
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout());
		panel.add(segmentation);
		panel.add(prediction);
		return panel;
	}

	private void showSegmentation() {
		if (!segmentationModel.isTrained()) System.out.println("not trained yet");
		else {
			for (RandomAccessibleInterval<UnsignedByteType> segmentation : segmentationModel
				.getSegmentations(new UnsignedByteType()))
			{
				Views.iterable(segmentation).forEach(x -> x.mul(128));
				ImageJFunctions.show(segmentation);
			}
		}
	}

	private void showPrediction() {
		if (!segmentationModel.isTrained()) System.out.println("not trained yet");
		else {
			segmentationModel.getPredictions().forEach(ImageJFunctions::show);
		}
	}

	private static JFrame setupFrame() {
		JFrame frame = new JFrame();
		frame.setSize(500, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		return frame;
	}
}
