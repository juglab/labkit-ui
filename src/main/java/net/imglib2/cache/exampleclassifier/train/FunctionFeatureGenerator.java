package net.imglib2.cache.exampleclassifier.train;

import java.util.function.BiFunction;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class FunctionFeatureGenerator< S extends RealType< S >, T extends RealType< T > > implements FeatureGenerator< S, T >
{

	private final BiFunction< RandomAccessible< S >, RandomAccessibleInterval< T >, Void > functor;

	private final RandomAccessible< S > source;

	public FunctionFeatureGenerator( final BiFunction< RandomAccessible< S >, RandomAccessibleInterval< T >, Void > functor, final RandomAccessible< S > source ) throws Exception
	{
		super();
		this.functor = functor;
		this.source = source;
	}

	@Override
	public void generateFeatures( final RandomAccessibleInterval< T > target )
	{
		functor.apply( source, Views.hyperSlice( target, target.numDimensions() - 1, 0l ) );
	}

}
