package net.imglib2.atlas.classification;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.img.Img;
import net.imglib2.img.basictypeaccess.volatiles.VolatileShortAccess;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ShortType;

public class ClassifyingCellLoader implements CellLoader< ShortType >
{
	private final RandomAccessibleInterval< ? > original;

	private Classifier classifier;

	public void setClassifier( final Classifier classifier )
	{
		this.classifier = classifier;
	}

	public ClassifyingCellLoader(
			final RandomAccessibleInterval<?> original,
			final Classifier classifier)
	{
		this.original = original;
		this.classifier = classifier;
	}

	@Override
	public void load( final Img< ShortType > img ) throws Exception
	{
		classifier.segment(original, img );
	}
}