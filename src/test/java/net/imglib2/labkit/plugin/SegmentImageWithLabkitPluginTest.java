
package net.imglib2.labkit.plugin;

import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.trainable_segmentation.utils.SingletonContext;
import org.junit.Test;

public class SegmentImageWithLabkitPluginTest {

	@Test
	public void test() throws Exception {
		// setup
		ImageJ imageJ = new ImageJ(SingletonContext.getInstance());
		Dataset image = imageJ.scifio().datasetIO().open(fullPath("/blobs.tif"));
		String blobsModel = fullPath("/blobs.classifier");
		Dataset expectedImage = imageJ.scifio().datasetIO().open(fullPath("/blobs_segmentation.tif"));
		// process
		Object output = imageJ.command().run(SegmentImageWithLabkitPlugin.class, true, "input", image,
			"segmenter_file", blobsModel).get().getOutput("output");
		// test
		ImgLib2Assert.assertImageEquals(expectedImage, (Dataset) output, Object::equals);
	}

	private String fullPath(String name) {
		return SegmentImageWithLabkitPluginTest.class.getResource(
			name).getFile();
	}
}
