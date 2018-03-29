package net.imglib2.labkit;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.labeling.LabelingSerializer;
import net.imglib2.util.Intervals;
import org.scijava.Context;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class InitialLabeling {
	static Labeling initLabeling(InputImage inputImage, Context context, List<String> defaultLabels) {
		String filename = inputImage.getFilename();
		if(new File(filename + ".labeling").exists()) {
			try {
				return new LabelingSerializer(context).open(filename + ".labeling");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Interval interval = inputImage.interval();
		Labeling labeling = new Labeling(defaultLabels, askShrinkInterval(interval));
		labeling.setAxes(inputImage.axes());
		return labeling;
	}

	static private Interval askShrinkInterval(Interval interval) {
		if(interval.numDimensions() != 2)
			return interval;
		if(!consideredBig(interval))
			return interval;
		interval = new FinalInterval(interval);
		List<Interval> suggestions = new ArrayList<>();
		suggestions.add(interval);
		while(consideredBig(interval)) {
			interval = shrink(interval);
			suggestions.add(interval);
		}
		List<String> texts = suggestions.stream().map(i -> i.dimension(0) + "x" + i.dimension(1)).collect(Collectors.toList());
		Object selected = JOptionPane.showInputDialog(null, "Select resultion of the labeling",
				"Labekit", JOptionPane.PLAIN_MESSAGE, null, texts.toArray(), texts.get(texts.size() - 1));
		int index = texts.indexOf(selected);
		return (index >= 0) ? suggestions.get(index) : interval;
	}

	static private Interval shrink(Interval interval) {
		long[] dimensions = Intervals.dimensionsAsLongArray(interval);
		long[] newDimensions = LongStream.of(dimensions).map(x -> x / 2).toArray();
		return new FinalInterval(newDimensions);
	}

	static private boolean consideredBig(Interval interval) {
		return Intervals.numElements(interval) > 10000000;
	}
}
