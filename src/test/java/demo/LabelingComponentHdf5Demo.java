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

import mpicbg.spim.data.SpimDataException;
import sc.fiji.labkit.ui.BasicLabelingComponent;
import sc.fiji.labkit.ui.LabelingComponent;
import sc.fiji.labkit.ui.inputimage.SpimDataInputImage;
import sc.fiji.labkit.ui.models.ImageLabelingModel;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Demonstrates how to use {@link LabelingComponent} for an HDF5 image.
 */
public class LabelingComponentHdf5Demo {

	public static void main(String... args) throws SpimDataException {
		JFrame frame = initFrame();
		final String fn = LabelingComponentHdf5Demo.class.getResource("/export.xml").getPath();
		frame.add(initLabelingComponent(frame, fn));
		frame.setVisible(true);
	}

	private static JFrame initFrame() {
		JFrame frame = new JFrame();
		frame.setSize(400, 400);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return frame;
	}

	private static JComponent initLabelingComponent(JFrame frame,
		String filename)
	{
		ImageLabelingModel model = new ImageLabelingModel(new SpimDataInputImage(filename, 0));
		LabelingComponent labelingComponent = new LabelingComponent(frame, model);
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				labelingComponent.close();
			}
		});
		return labelingComponent;
	}

}
