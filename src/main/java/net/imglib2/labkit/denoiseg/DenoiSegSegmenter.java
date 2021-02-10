
package net.imglib2.labkit.denoiseg;

import de.csbdresden.denoiseg.command.DenoiSegTrainCommand;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;
import org.scijava.Context;
import org.scijava.command.CommandService;

import javax.swing.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class DenoiSegSegmenter implements Segmenter {

	private final CommandService commandService;

	public DenoiSegSegmenter(Context context) {
		commandService = context.getService(CommandService.class);
	}

	@Override
	public void editSettings(JFrame dialogParent,
		List<Pair<ImgPlus<?>, Labeling>> trainingData)
	{
		/*JPanel pane = new JPanel();
		if (!SwingUtilities.isEventDispatchThread()) {
			pane.add(new JLabel("Not EDT"));
		} else {
			pane.add(new JLabel("EDT alright"));
		}
		dialogParent = new JFrame();
		dialogParent.add(pane);
		dialogParent.pack();
		dialogParent.setVisible(true);*/
	}

	@Override
	public void train(List<Pair<ImgPlus<?>, Labeling>> trainingData) {
		// TODO

		//commandService.run(DenoiSegTrainCommand.class, true, new HashMap<String, Object>());

	}

	@Override
	public void segment(ImgPlus<?> image,
		RandomAccessibleInterval<? extends IntegerType<?>> outputSegmentation)
	{

	}

	@Override
	public void predict(ImgPlus<?> image,
		RandomAccessibleInterval<? extends RealType<?>> outputProbabilityMap)
	{

	}

	@Override
	public boolean isTrained() {
		return false;
	}

	@Override
	public void saveModel(String path) {

	}

	@Override
	public void openModel(String path) {

	}

	@Override
	public List<String> classNames() {
		return Collections.emptyList();
	}

	@Override
	public int[] suggestCellSize(ImgPlus<?> image) {
		return new int[0];
	}

	@Override
	public boolean requiresFixedCellSize() {
		return false;
	}
}
