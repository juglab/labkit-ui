package net.imglib2.labkit;

import net.imglib2.labkit.models.ColoredLabelsModel;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.panel.LabelPanel;

import javax.swing.*;

public class EnhancedLabelingComponent implements AutoCloseable
{

	private final JSplitPane panel;

	private final WrappedBdv wrappedBdv;

	public EnhancedLabelingComponent( JFrame dialogBoxOwner, ImageLabelingModel model ) {
		this.wrappedBdv = new WrappedBdv(dialogBoxOwner, model);
		JComponent leftPanel = new LabelPanel( dialogBoxOwner, new ColoredLabelsModel( model ), true ).getComponent();
		this.panel = initSplitPane( leftPanel, wrappedBdv.getComponent() );
	}

	private JSplitPane initSplitPane( JComponent left, JComponent right )
	{
		JSplitPane panel = new JSplitPane();
		panel.setSize(100, 100);
		panel.setOneTouchExpandable(true);
		panel.setLeftComponent( left );
		panel.setRightComponent( right );
		return panel;
	}

	public JComponent getComponent() {
		return panel;
	}

	@Override
	public void close()
	{
		wrappedBdv.close();
	}
}
