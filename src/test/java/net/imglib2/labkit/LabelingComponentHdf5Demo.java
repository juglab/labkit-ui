package net.imglib2.labkit;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.realtransform.AffineTransform3D;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

public class LabelingComponentHdf5Demo
{
	public static void main(String... args) throws SpimDataException
	{
		JFrame frame = initFrame();
		final String fn = "/home/arzt/Documents/Datasets/Mouse Brain/hdf5/export.xml";
		final SpimDataMinimal spimData = new XmlIoSpimDataMinimal().load( fn );
		frame.add( initLabelingComponent( frame, spimData ) );
		frame.setVisible( true );
	}

	private static JFrame initFrame()
	{
		JFrame frame = new JFrame();
		frame.setSize(400, 400);
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		return frame;
	}

	private static JComponent initLabelingComponent( JFrame frame, SpimDataMinimal spimData )
	{
		ImageLabelingModel model = initModel( spimData );
		LabelingComponent labelingComponent = new LabelingComponent( frame, model );
		frame.addWindowListener( new WindowAdapter()
		{
			@Override public void windowClosing( WindowEvent e )
			{
				labelingComponent.close();
			}
		} );
		return labelingComponent.getComponent();
	}

	private static ImageLabelingModel initModel( SpimDataMinimal spimData )
	{
		// TODO simplify the creation of an ImageLabelingModel
		BdvShowable wrap = BdvShowable.wrap( spimData );
		Labeling labeling = new Labeling( Arrays.asList("fg","bg"), wrap.interval());
		boolean isTimeSeries = false;
		AffineTransform3D transformation = getVoxelTransformation( wrap.voxelSize() );
		return new ImageLabelingModel( wrap, transformation, labeling, isTimeSeries );
	}

	private static AffineTransform3D getVoxelTransformation( VoxelDimensions voxelSize )
	{
		double[] values = new double[Math.max( 3, voxelSize.numDimensions() ) ];
		voxelSize.dimensions( values );
		AffineTransform3D transformation = new AffineTransform3D();
		transformation.set(
				values[0], 0, 0, 0,
				0, values[1], 0, 0,
				0, 0, values[2], 0
			);
		return transformation;
	}
}
