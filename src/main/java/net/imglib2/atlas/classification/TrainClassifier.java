package net.imglib2.atlas.classification;

import java.awt.event.ActionEvent;

import net.imglib2.atlas.MainFrame;
import net.imglib2.atlas.labeling.Labeling;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class TrainClassifier< F extends RealType< F > > extends AbstractNamedAction
{

	private Labeling groundTruth;

	public TrainClassifier(
			final MainFrame.Extensible extensible,
			final Classifier classifier,
			final Labeling groundTruth,
			final RandomAccessibleInterval<F> features
	)
	{
		super( "Train Classifier" );
		this.classifier = classifier;
		this.groundTruth = groundTruth;
		this.features = features;
		extensible.addAction(this, "ctrl shift T");
	}

	private final Classifier classifier;

	private final RandomAccessibleInterval< F > features;

	private boolean trainingSuccess = false;

	public boolean getTrainingSuccess()
	{
		return trainingSuccess;
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
		trainingSuccess = false;
		try
		{
			classifier.trainClassifier( Views.<F>collapse(features), groundTruth );
			trainingSuccess = true;
		}
		catch ( final Exception e1 )
		{
			System.out.println("Training was interrupted by exception:");
			e1.printStackTrace();
			trainingSuccess = false;
		}
	}

}
