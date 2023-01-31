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

import net.imglib2.RandomAccessibleInterval;
import sc.fiji.labkit.ui.Extensible;
import sc.fiji.labkit.ui.MenuBar;
import sc.fiji.labkit.ui.models.LabelingModel;
import sc.fiji.labkit.ui.labeling.Labeling;
import sc.fiji.labkit.ui.labeling.LabelingSerializer;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.util.Cast;

import java.io.IOException;

/**
 * Implements the menu items for open, and saving the labeling, as well as ex
 * 
 * @author Matthias Arzt
 */
public class LabelingIoAction extends AbstractFileIoAction {

	private final LabelingModel labelingModel;
	private final LabelingSerializer serializer;

	public LabelingIoAction(Extensible extensible,
		LabelingModel labelingModel)
	{
		super(extensible, AbstractFileIoAction.LABELING_FILTER,
			AbstractFileIoAction.TIFF_FILTER);
		this.labelingModel = labelingModel;
		serializer = new LabelingSerializer(extensible.context());
		initSaveAction(MenuBar.LABELING_MENU, "Save Labeling ...", 2,
			new Action<Void>()
			{

				@Override
				public String suggestedFile() {
					return LabelingIoAction.this.labelingModel.defaultFileName();
				}

				@Override
				public void run(Void ignore, String filename) throws Exception {
					serializer.save(labelingModel.labeling().get(), filename);
				}
			}, "ctrl S");
		initOpenAction(MenuBar.LABELING_MENU, "Open Labeling ...", 1, this::open,
			"ctrl O");
		extensible.addMenuItem(MenuBar.LABELING_MENU, "Show Labeling in ImageJ", 3,
			ignore -> showLabelingInImageJ(), null, "");
	}

	private void showLabelingInImageJ() {
		RandomAccessibleInterval<? extends IntegerType<?>> img = labelingModel
			.labeling().get().getIndexImg();
		ImageJFunctions.show(Cast.unchecked(img), "Labeling");
	}

	private void open(Void ignore, String filename) throws IOException {
		Labeling labeling = serializer.open(filename);
		labelingModel.labeling().set(labeling);
	}
}
