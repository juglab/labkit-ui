
package net.imglib2.labkit;

import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labkit.inputimage.DefaultInputImage;
import net.imglib2.labkit.labeling.Labeling;
import org.junit.Test;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class InitialLabelingTest {

	@Test
	public void testInitialLabeling() throws IOException {
		File empty = File.createTempFile("labkit-InitialLabelingTest-",
			".czi.labeling");
		try {
			DefaultInputImage inputImage = new DefaultInputImage(ArrayImgs
				.unsignedBytes(2, 3));
			inputImage.setFilename(empty.getAbsolutePath().replace(".labeling", ""));
			Context context = new Context();
			List<String> defaultLabels = Collections.emptyList();
			Labeling result = InitialLabeling.initLabeling(inputImage, context,
				defaultLabels);
			assertNotNull(result);
		}
		finally {
			empty.delete();
		}
	}
}
