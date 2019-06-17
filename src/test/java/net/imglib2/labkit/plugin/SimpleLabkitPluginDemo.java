package net.imglib2.labkit.plugin;

import net.imagej.ImageJ;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import org.junit.Test;
import org.scijava.command.CommandModule;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class SimpleLabkitPluginDemo {

	@Test
	public void run() throws ExecutionException, InterruptedException {
		ImageJ ij = new ImageJ();

		long[] dims = new long[]{10,10};

		//create input image, set values to zero, left top quarter to one
		Img<DoubleType> input = ij.op().create().img(dims);
		RandomAccess<DoubleType> inputRA = input.randomAccess();
		for (int i = 0; i < dims[0]; i++) {
			for (int j = 0; j < dims[1]; j++) {
				inputRA.setPosition(new int[]{i,j});
				if(i < 5 && j < 5) {
					inputRA.get().setOne();
				} else {
					inputRA.get().setZero();
				}
			}
		}

		//create labeling with first pixel labeled one, rest zero
		Img<ByteType> labeling = ij.op().convert().int8(ij.op().create().img(dims));
		labeling.forEach(pixel -> pixel.setZero());
		labeling.firstElement().setReal(2.0);
		RandomAccess<ByteType> labelingRA = labeling.randomAccess();
		labelingRA.setPosition(new int[]{(int) (dims[0]-1), (int) (dims[1]-1)});
		labelingRA.get().setOne();

		ij.ui().show(input);
		ij.ui().show(labeling);

		// run labkit command
		CommandModule result = ij.command().run(SimpleLabkitPlugin.class, true, "input", input, "labeling", labeling).get();
		Img output = (Img) result.getOutput("output");

		ij.ui().show("output", output);
		// result control
		assertEquals(dims[0], output.dimension(0));
		assertEquals(dims[1], output.dimension(1));
		RandomAccess<UnsignedByteType> outputRA = output.randomAccess();
		for (int i = 0; i < dims[0]; i++) {
			for (int j = 0; j < dims[1]; j++) {
				outputRA.setPosition(new int[]{i,j});
				if(i < 5 && j < 5) {
					assertEquals(1, outputRA.get().get());
				}else {
					assertEquals(0, outputRA.get().get());
				}
			}
		}
	}

	public static void main(String... args) throws ExecutionException, InterruptedException {
		new SimpleLabkitPluginDemo().run();
	}

}
