package net.imglib2.cache.exampleclassifier.train.classification;

public interface Classifier< INSTANCE, INSTANCES, LABELS >
{

	public void predictLabels( INSTANCES instances, LABELS labels ) throws Exception;

	public void trainClassifier( Iterable< INSTANCE > samples, int[] labels ) throws Exception;

}
