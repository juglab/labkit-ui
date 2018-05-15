package net.imglib2.labkit.actions;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import org.scijava.plugin.Parameter;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class BitmapImportExportAction extends AbstractFileIoAcion
{

	private final ImageLabelingModel model;

	private final Extensible extensible;

	@Parameter
	DatasetService datasetService;

	@Parameter
	DatasetIOService datasetIOService;

	public BitmapImportExportAction( Extensible extensible, ImageLabelingModel model )
	{
		super( extensible, AbstractFileIoAcion.TIFF_FILTER );
		this.extensible = extensible;
		this.model = model;
		extensible.context().inject( this );
		initOpenAction( "Import Label", "importLabel", this::importLabel, "" );
		initSaveAction( "Export Label", "exportLabel", this::exportLabel, "" );
	}

	private void importLabel( String filename ) throws IOException
	{
		RandomAccessibleInterval< RealType< ? > > image = datasetIOService.open( filename );
		Labeling labeling = model.labeling().get();
		if( ! Intervals.equals( image, labeling) )
			JOptionPane.showMessageDialog( extensible.dialogParent(),
					"The resolution of the image does not fit",
					"Import Label", JOptionPane.ERROR_MESSAGE );
		labeling.addLabel( "Label \"" + new File( filename ).getName() + "\"", toBoolType( image ) );
		model.labeling().notifier().forEach(l -> l.accept( labeling ));
	}

	private void exportLabel( String filename ) throws IOException
	{
		Labeling labeling = model.labeling().get();
		String selectedLabel = model.selectedLabel().get();
		RandomAccessibleInterval< BitType > bitmap = labeling.regions().get( selectedLabel );
		Dataset dataset = datasetService.create( toUnsignedByteType( bitmap ) );
		datasetIOService.save( dataset, filename );
	}

	private RandomAccessibleInterval< BoolType > toBoolType( RandomAccessibleInterval< RealType< ? > > image )
	{
		return Converters.convert( image, ( in, out ) -> out.set( in.getRealDouble() > 0.5 ), new BoolType() );
	}

	private RandomAccessibleInterval< UnsignedByteType > toUnsignedByteType( RandomAccessibleInterval< BitType > bitmap )
	{
		return Converters.convert( bitmap, ( in, out ) -> out.set( in.getInteger() ), new UnsignedByteType() );
	}
}
