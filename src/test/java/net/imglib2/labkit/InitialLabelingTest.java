
package net.imglib2.labkit;

import net.imagej.ImgPlus;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.inputimage.ImgPlusViewsOld;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.junit.Test;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class InitialLabelingTest {

	@Test
	public void testInitialLabeling() throws IOException {
		File empty = File.createTempFile("labkit-InitialLabelingTest-",
			".czi.labeling");
		try {
			ImgPlus<UnsignedByteType> image = ImgPlus.wrap(ArrayImgs.unsignedBytes(2, 3));
			image.setSource(empty.getAbsolutePath().replace(".labeling", ""));
			DatasetInputImage inputImage = new DatasetInputImage(image);
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
