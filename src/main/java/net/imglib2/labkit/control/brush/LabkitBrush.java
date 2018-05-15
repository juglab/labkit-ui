package net.imglib2.labkit.control.brush;

import bdv.viewer.ViewerPanel;
import net.imglib2.RealPoint;
import net.imglib2.labkit.models.BitmapModel;

/**
 * Created by random on 12.05.18.
 */
public class LabkitBrush {

    protected final ViewerPanel viewer;

    protected final BitmapModel model;

    private static final double[] PIXEL_CENTER_OFFSET = { 0.5, 0.5, 0.5 };

    public LabkitBrush(ViewerPanel viewer, BitmapModel model) {
        this.viewer = viewer;
        this.model = model;
    }

    protected RealPoint displayToImageCoordinates(final int x, final int y )
    {
        final RealPoint labelLocation = new RealPoint(3);
        labelLocation.setPosition( x, 0 );
        labelLocation.setPosition( y, 1 );
        labelLocation.setPosition( 0, 2 );
        viewer.displayToGlobalCoordinates( labelLocation );
        labelLocation.move( PIXEL_CENTER_OFFSET );
        return labelLocation;
    }

    protected void fireBitmapChanged()
    {
        model.fireBitmapChanged();
    }

}
