package net.imglib2.atlas.classification;

public interface Classifier< INSTANCE, INSTANCES, LABELS >
{

	public void predictLabels( INSTANCES instances, LABELS labels ) throws Exception;

	public void trainClassifier( Iterable< INSTANCE > samples, int[] labels ) throws Exception;

	public void saveClassifier( String path, boolean overwrite ) throws Exception;

	public void loadClassifier( String path ) throws Exception;

}
