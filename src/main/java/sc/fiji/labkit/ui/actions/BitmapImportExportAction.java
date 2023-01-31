/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.ui.actions;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.display.imagej.ImageJFunctions;
import sc.fiji.labkit.ui.Extensible;
import sc.fiji.labkit.ui.MenuBar;
import sc.fiji.labkit.ui.labeling.Label;
import sc.fiji.labkit.ui.labeling.Labeling;
import sc.fiji.labkit.ui.models.ImageLabelingModel;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import org.scijava.plugin.Parameter;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * Implements the "Import Bitmap...", and "Export Selected Label as Bitmap..."
 * menu items. As well as the "Export as Bitmap..." and "Show as Bitmap in
 * ImageJ..." menu item to the popup menu that is associated with the individual
 * label.
 */
public class BitmapImportExportAction extends AbstractFileIoAction {

	private final ImageLabelingModel model;

	@Parameter
	DatasetService datasetService;

	@Parameter
	DatasetIOService datasetIOService;

	public BitmapImportExportAction(Extensible extensible,
		ImageLabelingModel model)
	{
		super(extensible, AbstractFileIoAction.TIFF_FILTER);
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
