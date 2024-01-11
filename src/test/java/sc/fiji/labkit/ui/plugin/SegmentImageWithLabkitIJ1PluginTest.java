package sc.fiji.labkit.ui.plugin;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import ij.IJ;
import ij.ImagePlus;
import ij.macro.Interpreter;
import net.imglib2.img.VirtualStackAdapter;
import net.imglib2.test.ImgLib2Assert;
import org.junit.Test;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;

public class SegmentImageWithLabkitIJ1PluginTest {

	@Test
	public void test() throws IOException {
		SingletonContext.getInstance();
		String inputImage = fullPath("/blobs.tif");
		String blobsModel = fullPath("/blobs.classifier");
		String source = fullPath("/blobs_segmentation.tif");
		File outputImage = File.createTempFile("labkit-segmentation-test", ".tif");
		String macroTemplate = "close('*');\n" +
			"open('INPUT_TIF');\n" +
			"run('Segment Image With Labkit (IJ1)', 'segmenter_file=SEGMENTER_FILE use_gpu=false');\n" +
			"selectImage('segmentation of blobs.tif');\n" +
			"saveAs('Tiff', 'OUTPUT_TIF');\n" +
			"close('*');\n";
		String macro = macroTemplate
			.replace('\'', '"')
			.replace("INPUT_TIF", inputImage)
			.replace("SEGMENTER_FILE", blobsModel)
			.replace("OUTPUT_TIF", outputImage.getAbsolutePath());
		new Interpreter().run(macro);
		assertTrue(outputImage.exists());
		ImagePlus expected = IJ.openImage(source);
		ImagePlus result = IJ.openImage(outputImage.getAbsolutePath());
		ImgLib2Assert.assertImageEquals(VirtualStackAdapter.wrap(expected), VirtualStackAdapter.wrap(result), Object::equals);
	}

	private String fullPath(String name) {
		return SegmentImageWithLabkitPluginTest.class.getResource(
			name).getFile();
	}
}
