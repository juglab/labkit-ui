package net.imglib2.atlas.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;

import org.scijava.ui.behaviour.util.AbstractNamedAction;

import bdv.viewer.ViewerPanel;
import net.imglib2.atlas.classification.Classifier;
import net.imglib2.atlas.classification.TrainClassifier;

public class DeserializeClassifier extends AbstractNamedAction
{

	private final ViewerPanel viewer;

	private final Classifier classifier;

	public DeserializeClassifier(final ViewerPanel viewer, final Classifier classifier)
	{
		super( "Load Classifier" );
		this.viewer = viewer;
		this.classifier = classifier;
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
		synchronized ( viewer )
		{
			final JFileChooser fileChooser = new JFileChooser();
			final int returnVal = fileChooser.showOpenDialog( viewer );
			if ( returnVal == JFileChooser.APPROVE_OPTION )
				try
			{
					classifier.loadClassifier( fileChooser.getSelectedFile().getAbsolutePath() );
			}
			catch ( final Exception e1 )
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
	}

}
