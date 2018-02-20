package net.imglib2.labkit;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvHandlePanel;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.ByteType;
import org.junit.Test;
import org.scijava.Context;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GarbageCollectionTest
{

	private final Context context = new Context();

	@Test
	public void testButton() throws InterruptedException
	{
		showAndCloseManyJFrames( this::addButton );
	}

	// NB: Bdv seems to keep the JFrames from being garbage collected.
	@Test
	public void testBdv() throws InterruptedException
	{
		showAndCloseManyJFrames( this::addBigDataViewer );
	}

	@Test
	public void testSegmentationComponent() throws InterruptedException
	{
		showAndCloseManyJFrames( this::addSegmentationComponent );
	}

	private void showAndCloseManyJFrames( Consumer< JFrame > componentAdder ) throws InterruptedException
	{
		int n = 5;
		for ( int i = 0; i < n; i++ )
		{
			System.out.println( "Step " + i + " of " + n );
			showAndCloseJFrames( componentAdder );
		}
	}

	private void showAndCloseJFrames( Consumer< JFrame > componentAdder ) throws InterruptedException
	{
		List<JFrame> frames = new ArrayList<>();
		for ( int i = 0; i < 3; i++ )
			frames.add( showJFrame( componentAdder ) );
		Thread.sleep( 1000 );
		for ( JFrame frame : frames )
			closeJFrame(frame);
	}

	private JFrame showJFrame( Consumer< JFrame > componentAdder )
	{
		JFrame frame = new JFrame() {
			private byte[] data = allocateMemory();
		};
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		frame.setSize(500,500);
		componentAdder.accept( frame );
		frame.setVisible( true );
		return frame;
	}

	private byte[] allocateMemory()
	{
		return new byte[ boundedConvertToInt( Runtime.getRuntime().maxMemory() / 10 ) ];
	}

	private void closeJFrame( JFrame frame )
	{
		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	}

	private void addBigDataViewer( JFrame frame )
	{
		BdvHandle handle = new BdvHandlePanel( frame, Bdv.options() );
		BdvFunctions.show( createaImage(), "Image", Bdv.options().addTo( handle ));
		frame.add(handle.getViewerPanel());
		frame.addWindowListener( new WindowAdapter()
		{
			@Override public void windowClosed( WindowEvent e )
			{
				handle.close();
			}
		} );
	}

	private void addSegmentationComponent( JFrame frame ) {
		SegmentationComponent component = new SegmentationComponent( context, frame, createaImage(), false );
		frame.add(component.getComponent());
		frame.addWindowListener( new WindowAdapter()
		{
			@Override public void windowClosed( WindowEvent e )
			{
				component.close();
			}
		} );
	}

	private RandomAccessibleInterval< ByteType > createaImage()
	{
		return ArrayImgs.bytes( 10, 10 );
	}

	private void addButton( JFrame frame )
	{
		frame.add(new JButton("Hello World!"));
	}

	private int boundedConvertToInt( long dataSize )
	{
		return ( int ) Math.max( Integer.MIN_VALUE, Math.min( Integer.MAX_VALUE, dataSize ) );
	}
}
