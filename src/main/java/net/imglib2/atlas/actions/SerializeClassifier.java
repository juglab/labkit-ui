package net.imglib2.atlas.actions;

import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;

import org.scijava.ui.behaviour.util.AbstractNamedAction;

import bdv.viewer.ViewerPanel;
import net.imglib2.atlas.classification.Classifier;

public class SerializeClassifier extends AbstractNamedAction
{

	private final ViewerPanel viewer;

	private final Classifier< ?, ?, ? > classifier;

	public SerializeClassifier( final String name, final ViewerPanel viewer, final Classifier< ?, ?, ? > classifier )
	{
		super( "Save Classifier" );
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
					classifier.saveClassifier( fileChooser.getSelectedFile().getAbsolutePath(), true );
				}
				catch ( final Exception e1 )
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
		}
	}

}
