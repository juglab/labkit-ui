package net.imglib2.atlas;

import ij.ImagePlus;

import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;

public class PaintLabelsAndTrain
{

	public static void em()
	{

		final String imgPath = System.getProperty( "user.home" ) + "/Downloads/epfl-em/training.tif";
		final Img< UnsignedByteType > rawImg = ImageJFunctions.wrapByte( new ImagePlus( imgPath ) );
		final long[] dimensions = Intervals.dimensionsAsLongArray( rawImg );
		final int[] cellDimensions = new int[] { 128, 128, 2 };
		final CellGrid grid = new CellGrid( dimensions, cellDimensions );

		new MainFrame().trainClassifier(AtlasUtils.copyUnsignedBytes(rawImg), grid, true);
	}

	public static void boats()
	{
		final String imgPath = System.getProperty( "user.home" ) + "/Documents/boats.tif";
		final Img< UnsignedByteType > rawImg = ImageJFunctions.wrapByte( new ImagePlus( imgPath ) );
		final long[] dimensions = Intervals.dimensionsAsLongArray( rawImg );
		final int[] cellDimensions = new int[] { 128, 128 };
		final CellGrid grid = new CellGrid( dimensions, cellDimensions );

		new MainFrame().trainClassifier(AtlasUtils.copyUnsignedBytes(rawImg), grid, false);
	}

	public static void lung() {
		final String imgPath = "/home/arzt/Documents/20170804_LungImages/2017_08_03__0004.jpg";
		final Img<ARGBType> rawImg = ImageJFunctions.wrap( new ImagePlus( imgPath ) );
		final long[] dimensions = Intervals.dimensionsAsLongArray( rawImg );
		final int[] cellDimensions = new int[] { 128, 128 };
		final CellGrid grid = new CellGrid( dimensions, cellDimensions );

		new MainFrame().trainClassifier(rawImg, grid, false);
	}

	public static void main( final String[] args ) {
		lung();
	}
}
