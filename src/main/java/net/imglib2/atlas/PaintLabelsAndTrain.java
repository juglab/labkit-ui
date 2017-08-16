package net.imglib2.atlas;

import java.io.IOException;

import ij.ImagePlus;

import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;

public class PaintLabelsAndTrain
{

	public static void em()
	{

		final String imgPath = System.getProperty( "user.home" ) + "/Documents/epfl-em/training.tif";
		final Img< UnsignedByteType > rawImg = ImageJFunctions.wrapByte( new ImagePlus( imgPath ) );
		final long[] dimensions = Intervals.dimensionsAsLongArray( rawImg );
		final int[] cellDimensions = new int[] { 128, 128, 2 };
		final CellGrid grid = new CellGrid( dimensions, cellDimensions );


		final ArrayImg< UnsignedByteType, ByteArray > rawData = ArrayImgs.unsignedBytes( dimensions );
		for ( final Pair< UnsignedByteType, UnsignedByteType > p : Views.interval( Views.pair( rawImg, rawData ), rawImg ) )
			p.getB().set( p.getA() );

		new MainFrame().trainClassifier( rawData, grid, true);
	}

	public static void main( final String[] args )
	{

		final String imgPath = System.getProperty( "user.home" ) + "/Documents/boats.tif";
		final Img< UnsignedByteType > rawImg = ImageJFunctions.wrapByte( new ImagePlus( imgPath ) );
		final long[] dimensions = Intervals.dimensionsAsLongArray( rawImg );
		final int[] cellDimensions = new int[] { 128, 128 };
		final CellGrid grid = new CellGrid( dimensions, cellDimensions );

		final ArrayImg< UnsignedByteType, ByteArray > rawData = ArrayImgs.unsignedBytes( dimensions );
		for ( final Pair< UnsignedByteType, UnsignedByteType > p : Views.interval( Views.pair( rawImg, rawData ), rawImg ) )
			p.getB().set( p.getA() );

		new MainFrame().trainClassifier( rawData, grid, false);
	}
}
