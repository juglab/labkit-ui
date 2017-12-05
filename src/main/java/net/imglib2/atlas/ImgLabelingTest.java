package net.imglib2.atlas;

import net.imglib2.Cursor;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.*;
import net.imglib2.type.numeric.integer.IntType;

import java.util.StringJoiner;

/**
 * @author Matthias Arzt
 */
public class ImgLabelingTest {

	private static final int BACKGROUND = 0;

	private static Img<IntType> initCachedLabelsImg(long[] imgDimensions, int[] cellDimensions) {
		final DiskCachedCellImgOptions labelsOpt = DiskCachedCellImgOptions.options().cellDimensions( cellDimensions ).dirtyAccesses( true );
		final DiskCachedCellImgFactory< IntType > labelsFac = new DiskCachedCellImgFactory<>( labelsOpt );
		CellLoader<IntType> loader = target -> target.forEach(x -> x.set(BACKGROUND));
		return labelsFac.create( imgDimensions , new IntType(), loader);
	}

	public static void main(String... args) {
		Img<IntType> img = initCachedLabelsImg(new long[]{1000, 1000, 1000}, new int[]{10, 10, 10});
		ImgLabeling<String, ?> labeling = new ImgLabeling<>(img);
		RandomAccess<LabelingType<String>> ra = labeling.randomAccess();
		ra.setPosition(new long[]{42, 42, 42});
		ra.get().add("A");
		LabelRegions<String> regions = new LabelRegions<>(labeling);
		LabelRegion<String> region = regions.getLabelRegion("A");
		Cursor<?> cursor = img.cursor();
		while(cursor.hasNext()) {
			cursor.fwd();
			printPosition(cursor);
		}
		System.out.println("finished");
	}

	private static void printPosition(Localizable cursor) {
		StringJoiner joiner = new StringJoiner(", ");
		for (int i = 0; i < cursor.numDimensions(); i++)
			joiner.add(Long.toString(cursor.getLongPosition(i)));
		System.out.println(joiner);
	}
}
