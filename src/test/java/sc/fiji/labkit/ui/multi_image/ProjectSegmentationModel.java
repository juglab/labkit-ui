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

package sc.fiji.labkit.ui.multi_image;

import net.imagej.ImgPlus;
import sc.fiji.labkit.ui.labeling.Labeling;
import sc.fiji.labkit.ui.models.ExtensionPoints;
import sc.fiji.labkit.ui.models.ImageLabelingModel;
import sc.fiji.labkit.ui.project.LabeledImage;
import sc.fiji.labkit.ui.project.LabkitProjectModel;
import sc.fiji.labkit.ui.models.SegmentationItem;
import sc.fiji.labkit.ui.models.SegmentationModel;
import sc.fiji.labkit.ui.models.SegmenterListModel;
import sc.fiji.labkit.ui.segmentation.weka.PixelClassificationPlugin;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.scijava.Context;

import java.util.AbstractList;
import java.util.List;

/**
 * ProjectSegmentationModel is a {@link SegmentationModel}, that allows to
 * change the {@link LabeledImage} it works with.
 */
public class ProjectSegmentationModel implements SegmentationModel {

	private final Context context;

	private final LabkitProjectModel projectModel;

	private ImageLabelingModel imageLabelingModel;

	private final SegmenterListModel segmenterList;

	private LabeledImage selectedImage;

	public ProjectSegmentationModel(LabkitProjectModel projectModel) {
		this.context = projectModel.context();
		this.projectModel = projectModel;
		this.segmenterList = initSegmenterListModel(projectModel.segmenterFiles());
	}

	@Override
	public Context context() {
		return context;
	}

	@Override
	public ImageLabelingModel imageLabelingModel() {
		return imageLabelingModel;
	}

	@Override
	public SegmenterListModel segmenterList() {
		return segmenterList;
	}

	public LabkitProjectModel projectModel() {
		return projectModel;
	}

	private SegmenterListModel initSegmenterListModel(List<String> segmenters) {
		SegmenterListModel segmenterListModel = new SegmenterListModel(context, new ExtensionPoints());
		segmenterListModel.trainingData().set(new TrainingData());
		for (String filename : segmenters) {
			SegmentationItem segmentationItem = segmenterListModel.addSegmenter(PixelClassificationPlugin
				.create());
			segmentationItem.openModel(filename);
		}
		return segmenterListModel;
	}

	public void setSelectedImage(LabeledImage image) {
		if (image == selectedImage)
			return;
		if (selectedImage != null) {
			selectedImage.close();
		}
		this.selectedImage = image;
		imageLabelingModel = image.open();
	}

	private class TrainingData extends AbstractList<Pair<ImgPlus<?>, Labeling>> {

		@Override
		public Pair<ImgPlus<?>, Labeling> get(int index) {
			LabeledImage imageItem = projectModel.labeledImages().get(index);
			if (imageItem == selectedImage) {
				ImgPlus<?> image = imageLabelingModel.imageForSegmentation().get();
				Labeling labeling = imageLabelingModel.labeling().get();
				return new ValuePair<>(image, labeling);
			}
			ImageLabelingModel ilm = imageItem.snapshot();
			return new ValuePair<>(ilm.imageForSegmentation().get(), ilm.labeling().get());
		}

		@Override
		public int size() {
			return projectModel.labeledImages().size();
		}
	}
}
