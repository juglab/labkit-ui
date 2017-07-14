package net.imglib2.atlas;

import net.imglib2.cache.img.CellLoader;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

public class FeatureGeneratorLoader< S extends RealType< S >, T extends RealType< T > > implements CellLoader< T >
{
	private final FeatureGenerator< S, T > generator;

	public FeatureGeneratorLoader(
			final FeatureGenerator<S, T> generator)
	{
		this.generator = generator;
	}

	@Override
	public void load( final Img< T > img ) throws Exception
	{
		final FeatureGenerator< S, T > generator = this.generator.copy();

		generator.generateFeatures( img );
	}
}