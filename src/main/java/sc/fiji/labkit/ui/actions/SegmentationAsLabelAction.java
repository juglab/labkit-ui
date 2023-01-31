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

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.type.numeric.IntegerType;
import sc.fiji.labkit.ui.Extensible;
import sc.fiji.labkit.ui.MenuBar;
import sc.fiji.labkit.ui.labeling.Label;
import sc.fiji.labkit.ui.labeling.Labeling;
import sc.fiji.labkit.ui.models.Holder;
import sc.fiji.labkit.ui.models.ImageLabelingModel;
import sc.fiji.labkit.ui.models.SegmentationItem;
import sc.fiji.labkit.ui.models.SegmentationModel;
import sc.fiji.labkit.ui.models.SegmentationResultsModel;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.logic.BitType;
import net.imglib2.view.Views;

import javax.swing.*;
import java.util.List;

/**
 * Implements the create label form segmentation menu item.
 *
 * @author Matthias Arzt
 */
public class SegmentationAsLabelAction {

	private final ImageLabelingModel labelingModel;
	private final Holder<? extends SegmentationItem> selectedSegmenter;

	public SegmentationAsLabelAction(
		Extensible extensible, SegmentationModel segmentationModel)
	{
		this.labelingModel = segmentationModel.imageLabelingModel();
		this.selectedSegmenter = segmentationModel.segmenterList().selectedSegmenter();
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU,
			"Create Label from Segmentation ...", 400, this::addSegmentationAsLabel,
			null, null);
	}

	private void addSegmentationAsLabel(SegmentationItem segmentationItem) {
		SegmentationResultsModel selectedResults = segmentationItem.results(labelingModel);
		List<String> labels = selectedResults.labels();
		String selected = (String) JOptionPane.showInputDialog(null,
			"Select label to be added", "Add Segmentation as Labels ...",
			JOptionPane.PLAIN_MESSAGE, null, labels.toArray(), labels.get(labels
				.size() - 1));
		int index = labels.indexOf(selected);
		if (index < 0) return;
		addLabel(selected, index, selectedResults.segmentation());
	}

	private void addLabel(String selected, int index,
		RandomAccessibleInterval<? extends IntegerType<?>> segmentation)
	{
		Converter<IntegerType<?>, BitType> converter = (in, out) -> out.set(in
			.getInteger() == index);
		RandomAccessibleInterval<BitType> result = Converters.convert(segmentation,
			converter, new BitType());
		Holder<Labeling> labelingHolder = labelingModel.labeling();
		addLabel(labelingHolder.get(), "segmented " + selected, result);
		labelingHolder.notifier().notifyListeners();
	}

	// TODO move to better place
	private static void addLabel(Labeling labeling, String name,
		RandomAccessibleInterval<BitType> mask)
	{
		Cursor<BitType> cursor = Views.iterable(mask).cursor();
		Label label = labeling.addLabel(name);
		RandomAccess<LabelingType<Label>> ra = labeling.randomAccess();
		while (cursor.hasNext()) {
			boolean value = cursor.next().get();
			if (value) {
				ra.setPosition(cursor);
				ra.get().add(label);
			}
		}
	}
}
