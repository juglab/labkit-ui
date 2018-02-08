package net.imglib2.labkit.models;

import bdv.viewer.ViewerPanel;
import net.imglib2.realtransform.AffineTransform3D;

public class TransformationModel
{
	private ViewerPanel viewerPanel;

	public void initialize(ViewerPanel viewerPanel) {
		this.viewerPanel = viewerPanel;
	}

	public int width()
	{
		return viewerPanel == null ? 100 : viewerPanel.getWidth();
	}

	public int height()
	{
		return viewerPanel == null ? 100 : viewerPanel.getHeight();
	}

	public void setTransformation( AffineTransform3D transformation )
	{
		if(viewerPanel != null)
			viewerPanel.setCurrentViewerTransform( transformation );
	}
}
