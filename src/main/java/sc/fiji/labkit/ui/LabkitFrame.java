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

package sc.fiji.labkit.ui;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.legacy.ui.LegacyApplicationFrame;
import org.scijava.ui.ApplicationFrame;
import org.scijava.ui.UIService;
import org.scijava.widget.UIComponent;
import sc.fiji.labkit.ui.inputimage.DatasetInputImage;
import sc.fiji.labkit.ui.inputimage.InputImage;
import sc.fiji.labkit.ui.models.DefaultSegmentationModel;
import sc.fiji.labkit.ui.models.SegmentationModel;
import sc.fiji.labkit.ui.utils.Notifier;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;
import org.scijava.Context;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 * The main Labkit window. (This window allows to segment a single image. It has
 * to be distinguished from the LabkitProjectFrame, which allows to operation on
 * multiple images.) The window only contains a {@link SegmentationComponent}
 * and shows the associated main menu.
 *
 * @author Matthias Arzt
 */
public class LabkitFrame {

	private final JFrame frame = initFrame();

	private final Notifier onCloseListeners = new Notifier();

	public static LabkitFrame showForFile(Context context,
		final String filename)
	{
		if (context == null)
			context = SingletonContext.getInstance();
		Dataset dataset = openDataset(context, filename);
		return showForImage(context, new DatasetInputImage(dataset));
	}

	private static Dataset openDataset(Context context, String filename) {
		try {
			return context.service(DatasetIOService.class).open(filename);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static LabkitFrame showForImage(Context context,
		final InputImage inputImage)
	{
		if (context == null)
			context = SingletonContext.getInstance();
		final SegmentationModel model = new DefaultSegmentationModel(context, inputImage);
		model.imageLabelingModel().labeling().set(InitialLabeling.initialLabeling(context, inputImage));
		return show(model, inputImage.imageForSegmentation().getName());
	}

	public static LabkitFrame show(final SegmentationModel model,
		final String title)
	{
		return new LabkitFrame(model, title);
	}

	private LabkitFrame(final SegmentationModel model,
		final String title)
	{
		SegmentationComponent segmentationComponent = initSegmentationComponent(
			model);
		setTitle(title);
		frame.setIconImage(getImageJIcon(model.context()));
		frame.setJMenuBar(new MenuBar(segmentationComponent::createMenu));
		frame.setVisible(true);
	}

	private Image getImageJIcon(Context context) {
		try {
			// NB: get ImageJ icon form the main UI window
			UIService uiService = context.service(UIService.class);
			ApplicationFrame applicationFrame = uiService.getDefaultUI().getApplicationFrame();
			if (applicationFrame instanceof LegacyApplicationFrame)
				return ((LegacyApplicationFrame) applicationFrame).getComponent().getIconImage();
			if (applicationFrame instanceof Frame)
				return ((Frame) applicationFrame).getIconImage();
			return null;
		}
		catch (Exception e) {
			return null;
		}
	}

	private SegmentationComponent initSegmentationComponent(
		SegmentationModel segmentationModel)
	{
		SegmentationComponent segmentationComponent = new SegmentationComponent(
			frame, segmentationModel, false);
		frame.add(segmentationComponent);
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				segmentationComponent.close();
				onCloseListeners.notifyListeners();
			}
		});
		return segmentationComponent;
	}

	private JFrame initFrame() {
		JFrame frame = new JFrame();
		frame.setBounds(50, 50, 1200, 900);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return frame;
	}

	private void setTitle(String name) {
		if (name == null || name.isEmpty()) frame.setTitle("Labkit");
		else frame.setTitle("Labkit - " + name);
	}

	public Notifier onCloseListeners() {
		return onCloseListeners;
	}
}
