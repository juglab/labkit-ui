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

package sc.fiji.labkit.ui.project;

import sc.fiji.labkit.ui.models.DefaultHolder;
import sc.fiji.labkit.ui.models.Holder;
import sc.fiji.labkit.ui.utils.Notifier;
import org.scijava.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Labkit project, that contains a list of labeled images and
 * trained segmentation algorithms.
 */
public class LabkitProjectModel {

	private final Context context;

	private final String projectDirectory;

	private Holder<LabeledImage> selectedImage;

	private List<LabeledImage> labeledImageFiles;

	private List<String> segmenterFiles;

	private final Notifier changeNotifier = new Notifier();

	public LabkitProjectModel(Context context,
		String projectDirectory,
		List<LabeledImage> labeledImageFiles)
	{
		this.context = context;
		this.projectDirectory = projectDirectory;
		this.selectedImage = new DefaultHolder<>(labeledImageFiles.size() == 0 ? null
			: labeledImageFiles.get(
				0));
		this.labeledImageFiles = labeledImageFiles;
		this.segmenterFiles = new ArrayList<>();
		changeNotifier.addListener(this::onLabeledImagesChanged);
	}

	public String getProjectDirectory() {
		return projectDirectory;
	}

	public List<LabeledImage> labeledImages() {
		return labeledImageFiles;
	}

	public List<String> segmenterFiles() {
		return segmenterFiles;
	}

	public Context context() {
		return context;
	}

	public Notifier changeNotifier() {
		return changeNotifier;
	}

	public Holder<LabeledImage> selectedImage() {
		return selectedImage;
	}

	private void onLabeledImagesChanged() {
		if (labeledImageFiles.contains(selectedImage.get()))
			return;
		if (!labeledImageFiles.isEmpty())
			selectedImage.set(labeledImageFiles.get(0));
		else
			selectedImage.set(null);
	}
}
