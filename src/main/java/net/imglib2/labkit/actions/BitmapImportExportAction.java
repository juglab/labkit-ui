
package net.imglib2.labkit.actions;

import io.scif.FormatException;
import io.scif.services.DatasetIOService;
import io.scif.services.FormatService;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.labeling.Label;
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
import java.util.function.Consumer;

public class BitmapImportExportAction extends AbstractFileIoAction {

	private final ImageLabelingModel model;

	private final Extensible extensible;

	@Parameter
	DatasetService datasetService;

	@Parameter
	FormatService formatService;

	@Parameter
	DatasetIOService datasetIOService;

	public BitmapImportExportAction(Extensible extensible,
		ImageLabelingModel model)
	{
		super(extensible, AbstractFileIoAction.TIFF_FILTER);
		this.extensible = extensible;
		this.model = model;
		extensible.context().inject(this);
		initOpenAction("Import Bitmap ...", "importLabel", this::importLabel, "");
		initSaveAction("Export Selected Label as Bitmap ...", "exportLabel",
			this::exportLabel, "");
		extensible.addMenuItem(Label.LABEL_MENU, "Export as Bitmap ...",
			label -> openDialogAndThen("Export Label as Bitmap",
				JFileChooser.SAVE_DIALOG, filename -> exportLabel(label, filename)),
			null);
	}

	private void exportLabel(Label label, String filename) throws IOException {
		filename = autoAddExtension(filename);
		Labeling labeling = model.labeling().get();
		RandomAccessibleInterval<BitType> bitmap = labeling.getRegion(label);
		Dataset dataset = datasetService.create(toUnsignedByteType(bitmap));
		datasetIOService.save(dataset, filename);
	}

	private void importLabel(String filename) throws IOException {
		RandomAccessibleInterval<RealType<?>> image = datasetIOService.open(
			filename);
		Labeling labeling = model.labeling().get();
		if (!Intervals.equals(image, labeling)) JOptionPane.showMessageDialog(
			extensible.dialogParent(), "The resolution of the image does not fit",
			"Import Label", JOptionPane.ERROR_MESSAGE);
		labeling.addLabel("Label \"" + new File(filename).getName() + "\"",
			toBoolType(image));
		model.labeling().notifier().forEach(l -> l.accept(labeling));
	}

	private void exportLabel(String filename) throws IOException {
		Label label = model.selectedLabel().get();
		exportLabel(label, filename);
	}

	private String autoAddExtension(String filename) {
		return isSupported(filename) ? filename : filename + ".tif";
	}

	private boolean isSupported(String filename) {
		try {
			return null != formatService.getWriterByExtension(filename);
		}
		catch (FormatException e) {
			return false;
		}
	}

	private RandomAccessibleInterval<BoolType> toBoolType(
		RandomAccessibleInterval<RealType<?>> image)
	{
		return Converters.convert(image, (in, out) -> out.set(in
			.getRealDouble() > 0.5), new BoolType());
	}

	private RandomAccessibleInterval<UnsignedByteType> toUnsignedByteType(
		RandomAccessibleInterval<BitType> bitmap)
	{
		return Converters.convert(bitmap, (in, out) -> out.set(in.getInteger()),
			new UnsignedByteType());
	}
}
