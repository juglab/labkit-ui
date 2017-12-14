package net.imglib2.labkit.classification;

import java.util.Collections;
import java.util.function.Supplier;

import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.labeling.Labeling;

import net.imglib2.RandomAccessibleInterval;

public class TrainClassifier
{

	private Supplier<Labeling> labelingSupplier;

	public TrainClassifier(
			final Extensible extensible,
			final Classifier classifier,
			final Supplier<Labeling> labelingSupplier,
			final RandomAccessibleInterval<?> image
	)
	{
		this.classifier = classifier;
		this.labelingSupplier = labelingSupplier;
		this.image = image;
		extensible.addAction("Train Classifier", "trainClassifier", this::trainClassifier, "ctrl shift T");
	}

	private final Classifier classifier;

	private final RandomAccessibleInterval<?> image;

	private void trainClassifier()
	{
		try
		{
			classifier.train(Collections.singletonList(image), Collections.singletonList(labelingSupplier.get()));
		}
		catch ( final Exception e1 )
		{
			System.out.println("Training was interrupted by exception:");
			e1.printStackTrace();
		}
	}

}
