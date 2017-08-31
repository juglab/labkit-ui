package net.imglib2.atlas.actions;

import io.scif.img.ImgIOException;
import io.scif.img.ImgSaver;
import net.imglib2.atlas.MainFrame;
import net.imglib2.atlas.classification.PredictionLayer;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.ShortType;

import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author Matthias Arzt
 */
public class SegmentationSave extends AbstractSaveAndLoadAction {

	private final PredictionLayer predictionLayer;

	public SegmentationSave(MainFrame.Extensible extensible, PredictionLayer predictionLayer) {
		super(extensible, AbstractSaveAndLoadAction.TIFF_FILTER);
		this.predictionLayer = predictionLayer;
		initSaveAction("Save Segmentation", this::action, "");
	}

	private void action(String filename) throws IncompatibleTypeException, ImgIOException {
		Img<ShortType> img = predictionLayer.getPrediction();
		ImgSaver saver = new ImgSaver();
		saver.saveImg(filename, img);
	}
}
