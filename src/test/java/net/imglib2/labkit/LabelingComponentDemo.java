package net.imglib2.labkit;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.view.Views;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Random;

public class LabelingComponentDemo
{
	public static void main(String... args) {
		JFrame frame = new JFrame();
		frame.setSize(400, 400);
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		ImageLabelingModel model = initModel();
		frame.add( initLabelingComponent( frame, model ) );
		frame.setVisible( true );
	}

	private static ImageLabelingModel initModel()
	{
		RandomAccessibleInterval< ? extends NumericType< ? > > labelingIndexImage = ArrayImgs.bytes(100, 100, 100);
		Labeling labeling = new Labeling( Arrays.asList("fg","bg"), labelingIndexImage );
		boolean isTimeSeries = false;
		return new ImageLabelingModel( greenNoiseImage(100, 100, 100), labeling, isTimeSeries );
	}

	private static JComponent initLabelingComponent( JFrame frame, ImageLabelingModel model )
	{
		EnhancedLabelingComponent labelingComponent = new EnhancedLabelingComponent( frame, model );
		frame.addWindowListener( new WindowAdapter()
		{
			@Override public void windowClosing( WindowEvent e )
			{
				labelingComponent.close();
			}
		} );
		return labelingComponent.getComponent();
	}

	private static RandomAccessibleInterval< ARGBType > greenNoiseImage(long... dim)
	{
		RandomAccessibleInterval< ARGBType > backgroundImage = ArrayImgs.argbs(dim);
		final Random random = new Random( 42 );
		Views.iterable(backgroundImage).forEach( pixel -> pixel.set( ARGBType.rgba( 0, random.nextInt( 130 ), 0, 0 )) );
		ImageJFunctions.show( backgroundImage );
		return backgroundImage;
	}
}
