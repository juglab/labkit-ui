
package net.imglib2.labkit.v2.models;

import net.imagej.ImgPlus;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.labkit.labeling.Labeling;

/**
 * Represents an image, overlaid with a labeling.
 */
public class LabeledImageModel {

	private String imageFile;

	private String labelingFile;

	private String changedLabelingFile;

	private ImgPlus<?> imageForSegmentation;

	private BdvShowable imageForDisplaying;

	private Labeling labeling;

	// Getter & Setter ...
}
