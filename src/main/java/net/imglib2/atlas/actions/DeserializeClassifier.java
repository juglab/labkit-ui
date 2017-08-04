package net.imglib2.atlas.actions;

import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;

import net.imglib2.atlas.MainFrame;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import net.imglib2.atlas.classification.Classifier;

public class DeserializeClassifier extends AbstractNamedAction
{

	private final MainFrame.Extensible extensible;

	private final Classifier classifier;

	public DeserializeClassifier(final MainFrame.Extensible extensible, final Classifier classifier)
	{
		super( "Load Classifier" );
		this.extensible = extensible;
		this.classifier = classifier;
		extensible.addAction(this, "ctrl O");
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
		synchronized (extensible.viewerSync())
		{
			final JFileChooser fileChooser = new JFileChooser();
			final int returnVal = fileChooser.showOpenDialog(extensible.dialogParent());
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
