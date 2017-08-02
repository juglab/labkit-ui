package net.imglib2.atlas.classification;

import net.imglib2.RandomAccessible;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.composite.Composite;
import weka.classifiers.Classifier;

public class ProbabilityMapLoader< T extends RealType< T > > implements CellLoader< UnsignedShortType >
{
	private final CellGrid grid;

	private final RandomAccessible< ? extends Composite< T > > features;

	private Classifier classifier;

	private final int numFeatures;

	private final int numClasses;

	public void setClassifier( final Classifier classifier )
	{
		this.classifier = classifier;
	}

	public ProbabilityMapLoader(
			final CellGrid grid,
			final RandomAccessible< ? extends Composite< T > > features,
					final Classifier classifier,
					final int numFeatures,
					final int numClasses )
	{
		this.grid = grid;
		this.features = features;
		this.classifier = classifier;
		this.numFeatures = numFeatures;
		this.numClasses = numClasses;
	}

	@Override
	public void load( final Img< UnsignedShortType > img ) throws Exception
	{

	}
}