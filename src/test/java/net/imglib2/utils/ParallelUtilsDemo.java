
package net.imglib2.utils;

import bdv.export.ProgressWriter;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.img.Img;
import net.imglib2.labkit.utils.ParallelUtils;
import net.imglib2.labkit.utils.progress.SwingProgressWriter;
import net.imglib2.type.numeric.integer.ShortType;

public class ParallelUtilsDemo {

	public static void main(String... args) {
		ProgressWriter progress = new SwingProgressWriter(null, "In progress");
		CellLoader<ShortType> loader = cell -> Thread.sleep(1000);
		Img<ShortType> img = new DiskCachedCellImgFactory<>(new ShortType()).create(
			new long[] { 40, 40, 40 }, loader);
		ParallelUtils.populateCachedImg(img, progress);
		System.out.println("done");
	}

}
