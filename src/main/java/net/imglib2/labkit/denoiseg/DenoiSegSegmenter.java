
package net.imglib2.labkit.denoiseg;

import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

public class DenoiSegSegmenter implements Segmenter {

	@Override
	public void editSettings(JFrame dialogParent,
		List<Pair<ImgPlus<?>, Labeling>> trainingData)
	{

	}

	@Override
	public void train(List<Pair<ImgPlus<?>, Labeling>> trainingData) {
		// TODO
		Pair<ImgPlus<?>, Labeling> pair = trainingData.get(0);
		ImgPlus<?> image = pair.getA();
		Labeling labeling = pair.getB();
		ImageJFunctions.show((RandomAccessibleInterval) image);
		ImageJFunctions.show((RandomAccessibleInterval) labeling.getIndexImg());
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
