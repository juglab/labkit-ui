package net.imglib2.labkit.models;

import net.imglib2.Cursor;
import net.imglib2.labkit.color.ColorMap;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.roi.IterableRegion;
import net.imglib2.transform.integer.BoundingBox;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.ARGBType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColoredLabelsModel
{

	private final ImageLabelingModel model;

	private final Notifier< Runnable > listeners = new Notifier<>();

	public ColoredLabelsModel(ImageLabelingModel model) {
		this.model = model;
		model.labeling().notifier().add( l -> notifyListeners() );
		model.selectedLabel().notifier().add( s -> notifyListeners() );
	}

	private void notifyListeners()
	{
		listeners.forEach( Runnable::run );
	}

	// TODO: use a List instead of a Map to keep the Order
	public Map<String, ARGBType > items() {
		Map<String, ARGBType> result = new HashMap<>();
		ColorMap colors = model.colorMapProvider().colorMap();
		List<String> labels = model.labeling().get().getLabels();
		labels.forEach( label -> result.put( label, colors.getColor( label ) ) );
		return result;
	}

	public String selected() {
		return model.selectedLabel().get();
	}

	public void setSelected(String value) {
		model.selectedLabel().set( value );
	}

	public Notifier<Runnable> listeners() {
		return listeners;
	}

	public void addLabel() {
		Holder< Labeling > holder = model.labeling();
		Labeling labeling = holder.get();
		String label = suggestName(labeling.getLabels());
		if(label == null)
			return;
		labeling.addLabel(label);
		holder.notifier().forEach(l -> l.accept(labeling));
	}

	public void removeLabel(String label) {
		Holder< Labeling > holder = model.labeling();
		Labeling labeling = holder.get();
		holder.get().removeLabel( label );
		holder.notifier().forEach( l -> l.accept( labeling ) );
	}

	public void renameLabel(String label, String newLabel) {
		Holder< Labeling > holder = model.labeling();
		Labeling labeling = holder.get();
		holder.get().renameLabel( label, newLabel );
		holder.notifier().forEach( l -> l.accept( labeling ) );
	}

	public void setColor(String label, ARGBType newColor) {
		Holder< Labeling > holder = model.labeling();
		Labeling labeling = holder.get();
		model.colorMapProvider().colorMap().setColor(label, newColor);
		holder.notifier().forEach( l -> l.accept( labeling ) );
	}

	private String suggestName(List<String> labels) {
		for (int i = 1; i < 10000; i++) {
			String label = "Label " + i;
			if (!labels.contains(label))
				return label;
		}
		return null;
	}

	public void localizeLabel( final String label ) {
		final BoundingBox labelBox = getBoundingBox( model.labeling().get().iterableRegions().get( label ) );
		if ( labelBox != null ) {
			final AffineTransform3D transform = getTransformation( labelBox );
			model.transformationModel().setTransformation( transform );
		}
	}

	private AffineTransform3D getTransformation( BoundingBox labelBox )
	{
		final double[] screenSize = { model.transformationModel().width(), model.transformationModel().height() };
		final double scale = 0.5 * getBiggestScaleFactor( screenSize, labelBox );
		final double[] translate = getTranslation( screenSize, labelBox, scale );
		final AffineTransform3D transform = new AffineTransform3D();
		transform.scale( scale );
		transform.translate( translate );
		return transform;
	}

	private static double[] getTranslation( final double[] screenSize, final BoundingBox labelBox, final double labelScale ) {
		final double[] translate = new double[ 3 ];
		for ( int i = 0; i < Math.min( translate.length, labelBox.numDimensions() ); i++ ) {
			translate[ i ] = - ( labelBox.corner2[ i ] + labelBox.corner1[ i ] ) * labelScale / 2;
			if ( i < 2 ) {
				translate[ i ] += screenSize[ i ] / 2;
			}
		}
		return translate;
	}

	private static double getBiggestScaleFactor( final double[] screenSize, final BoundingBox labelBox ) {
		final Double[] scales = new Double[ 2 ];
		final double minLength = 20.0;
		for ( int i = 0; i < 2; i++ ) {
			scales[ i ] = screenSize[ i ] / Math.max( labelBox.corner2[ i ] - labelBox.corner1[ i ], minLength );
		}
		return Collections.min( Arrays.asList( scales ) );
	}

	private static BoundingBox getBoundingBox( IterableRegion< BitType > region )
	{
		int numDimensions = region.numDimensions();
		BoundingBox box = new BoundingBox( numDimensions );
		Cursor<?> cursor = region.cursor();
		if ( cursor.hasNext() ) {
			cursor.fwd();
			cursor.localize( box.corner1 );
			cursor.localize( box.corner2 );
		}
		else
			return null;
		while( cursor.hasNext() )	{
			cursor.fwd();
			for( int i = 0; i < numDimensions; i++){
				int pos = cursor.getIntPosition( i );
				box.corner1[ i ] = Math.min( box.corner1[ i ], pos);
				box.corner2[ i ] = Math.max( box.corner2[ i ], pos);
			}
		}
		return box;
	}
}
