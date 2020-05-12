
package net.imglib2.labkit;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvHandlePanel;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.models.DefaultSegmentationModel;
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
		DefaultSegmentationModel segmentationModel = new DefaultSegmentationModel(
			new DatasetInputImage(ArrayImgs.bytes(10, 10)), context);
		SegmentationComponent component = new SegmentationComponent(frame,
			segmentationModel, false);
		frame.add(component.getComponent());
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
