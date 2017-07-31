package net.imglib2.atlas.classification;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;
import net.imglib2.view.composite.Composite;

import java.util.Iterator;

public interface Classifier
{

	public void predictLabels(RandomAccessibleInterval<? extends Composite<? extends RealType<?>>> instances, RandomAccessibleInterval<? extends IntegerType<?>> labels ) throws Exception;

	public void trainClassifier(Iterator<Pair<Composite<? extends RealType<?>>, ? extends IntegerType<?>>> data) throws Exception;

	public boolean isTrained();

	public void saveClassifier( String path, boolean overwrite ) throws Exception;

	public void loadClassifier( String path ) throws Exception;

}
