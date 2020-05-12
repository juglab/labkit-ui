
package net.imglib2.labkit.segmentation;

import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;

import javax.swing.*;
import java.util.List;

public class ForwardingSegmenter implements Segmenter {

	private final Segmenter source;

	public ForwardingSegmenter(Segmenter source) {
		this.source = source;
	}

	@Override
	public void editSettings(JFrame dialogParent) {
		source.editSettings(dialogParent);
	}

	@Override
	public void train(List<Pair<ImgPlus<?>, Labeling>> trainingData) {
		source.train(trainingData);
	}

	@Override
	public void segment(ImgPlus<?> image,
		RandomAccessibleInterval<? extends IntegerType<?>> outputSegmentation)
	{
		source.segment(image, outputSegmentation);
	}

	@Override
	public void predict(ImgPlus<?> image,
		RandomAccessibleInterval<? extends RealType<?>> outputProbabilityMap)
	{
		source.predict(image, outputProbabilityMap);
	}

	@Override
	public boolean isTrained() {
		return source.isTrained();
	}

	@Override
	public void saveModel(String path) {
		source.saveModel(path);
	}

	@Override
	public void openModel(String path) {
		source.openModel(path);
	}

	@Override
	public List<String> classNames() {
		return source.classNames();
	}
}
