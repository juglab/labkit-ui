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

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvHandlePanel;
import net.imglib2.img.array.ArrayImgs;
import sc.fiji.labkit.ui.inputimage.DatasetInputImage;
import sc.fiji.labkit.ui.models.DefaultSegmentationModel;
import sc.fiji.labkit.ui.models.SegmentationModel;
import org.junit.Test;
import org.scijava.Context;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

import static org.junit.Assume.assumeFalse;

public class GarbageCollectionTest {

	private final Context context = new Context();

	@Test
	public void testBdv() throws InterruptedException {
		testGarbageCollection(this::addBigDataViewer);
	}

	private void addBigDataViewer(JFrame frame) {
		BdvHandle handle = new BdvHandlePanel(frame, Bdv.options());
		BdvFunctions.show(ArrayImgs.bytes(10, 10), "Image", Bdv.options().addTo(
			handle));
		frame.add(handle.getViewerPanel());
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				handle.close();
			}
		});
	}

	@Test
	public void testSegmentationComponent() throws InterruptedException {
		testGarbageCollection(this::addSegmentationComponent);
	}

	private void addSegmentationComponent(JFrame frame) {
		SegmentationModel segmentationModel = new DefaultSegmentationModel(
			context, new DatasetInputImage(ArrayImgs.bytes(10, 10)));
		SegmentationComponent component = new SegmentationComponent(frame,
			segmentationModel, false);
		frame.add(component);
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				component.close();
			}
		});
	}

	private void testGarbageCollection(Consumer<JFrame> componentAdder)
		throws InterruptedException
	{
		assumeFalse(GraphicsEnvironment.isHeadless());
		for (int i = 0; i < 8; i++)
			showAndCloseJFrame(componentAdder);
	}

	private void showAndCloseJFrame(Consumer<JFrame> componentAdder)
		throws InterruptedException
	{
		JFrame frame = showJFrame(componentAdder);
		Thread.sleep(200);
		closeJFrame(frame);
	}

	private JFrame showJFrame(Consumer<JFrame> componentAdder) {
		JFrame frame = new JFrame() {

			// NB: we link so much memory with this JFrame object, that only 4 or less
			// instances fit into memory.
			private byte[] data = allocateLargeAmountOfMemory();
		};
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(500, 500);
		componentAdder.accept(frame);
		frame.setVisible(true);
		return frame;
	}

	private void closeJFrame(JFrame frame) {
		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	}

	private byte[] allocateLargeAmountOfMemory() {
		return new byte[boundedConvertToInt(Runtime.getRuntime().maxMemory() / 5)];
	}

	private int boundedConvertToInt(long dataSize) {
		return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE,
			dataSize));
	}
}
