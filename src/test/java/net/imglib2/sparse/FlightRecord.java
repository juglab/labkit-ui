package net.imglib2.sparse;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;

public class FlightRecord {

	//RandomAccessibleInterval<IntType> image = //new SparseRandomAccessIntType(Intervals.createMinSize(0, 0, 0, 100, 100, 100));
	//		new NtreeImgFactory<IntType>().create(new long[]{100,100,100}, new IntType());
	RandomAccessibleInterval<IntType> image = ArrayImgs.ints(100,1000,1000);

	public void record()
	{
		for( IntegerType pixel : Views.iterable(image) )
			pixel.setOne();
	}

	public static void main(String... args) {
		new FlightRecord().record();
	}
}
