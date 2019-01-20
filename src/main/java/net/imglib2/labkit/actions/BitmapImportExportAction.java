
package net.imglib2.labkit.actions;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.MenuBar;
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

public class BitmapImportExportAction extends AbstractFileIoAction {

	private final ImageLabelingModel model;

	private final Extensible extensible;

	@Parameter
	DatasetService datasetService;

	@Parameter
	DatasetIOService datasetIOService;

	public BitmapImportExportAction(Extensible extensible,
		ImageLabelingModel model)
	{
		super(extensible, AbstractFileIoAction.TIFF_FILTER);
		this.extensible = extensible;
		this.model = model;
		extensible.context().inject(this);
		initOpenAction(MenuBar.LABELING_MENU, "Import Bitmap ...", 100,
			this::importLabel, "");
		final Action<Void> voidAction = this::exportLabel;
		initSaveAction(MenuBar.LABELING_MENU, "Export Selected Label as Bitmap ...",
			101, voidAction, "");
		extensible.addMenuItem(Label.LABEL_MENU, "Export as Bitmap ...", 400,
			label -> openDialogAndThen("Export Label as Bitmap",
				JFileChooser.SAVE_DIALOG, label, this::exportLabel), null, null);
		extensible.addMenuItem(Label.LABEL_MENU, "Show as Bitmap in ImageJ", 401,
			this::showLabel, null, "");
	}

	private void showLabel(Label label) {
		ImageJFunctions.show(model.labeling().get().getRegion(label), label.name());
	}

	private void exportLabel(Label label, String filename) throws IOException {
		Labeling labeling = model.labeling().get();
		RandomAccessibleInterval<BitType> bitmap = labeling.getRegion(label);
		Dataset dataset = datasetService.create(toUnsignedByteType(bitmap));
		datasetIOService.save(dataset, filename);
	}

	private void importLabel(Void ignore, String filename) throws IOException {
		RandomAccessibleInterval<RealType<?>> image = datasetIOService.open(
			filename);
		Labeling labeling = model.labeling().get();
		if (!Intervals.equals(image, labeling)) JOptionPane.showMessageDialog(
			extensible.dialogParent(), "The resolution of the image does not fit",
			"Import Label", JOptionPane.ERROR_MESSAGE);
		labeling.addLabel("Label \"" + new File(filename).getName() + "\"",
			toBoolType(image));
		model.labeling().notifier().notifyListeners();
	}

	private void exportLabel(Void ignore, String filename) throws IOException {
		Label label = model.selectedLabel().get();
		exportLabel(label, filename);
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
