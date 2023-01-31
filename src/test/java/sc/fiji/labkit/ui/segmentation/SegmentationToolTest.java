/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.ui.segmentation;

import bdv.export.ProgressWriterConsole;
import io.scif.services.DatasetIOService;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import org.junit.Test;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;

import java.io.IOException;

import static net.imglib2.test.ImgLib2Assert.assertImageEqualsIntegerType;
import static net.imglib2.test.ImgLib2Assert.assertImageEqualsRealType;
import static org.junit.Assert.assertEquals;

public class SegmentationToolTest {

	private final ImgPlus<?> image = openImage("/leaf.tif");

	@Test
	public void testSegment() {
		SegmentationTool tool = new SegmentationTool();
		tool.setUseGpu(false);
		tool.openModel(fullPath("/leaf.classifier"));
		((DefaultLinearAxis) image.axis(0)).setScale(0.7);
		((DefaultLinearAxis) image.axis(1)).setScale(0.7);
		ImgPlus<UnsignedByteType> segmentation = tool.segment(image);
		ImgPlus<?> expectedSegmentation = Cast.unchecked(openImage("/leaf_segmentation.tif"));
		assertImageEqualsIntegerType(Cast.unchecked(expectedSegmentation), segmentation);
		assertEquals(Axes.X, segmentation.axis(0).type());
		assertEquals(Axes.Y, segmentation.axis(1).type());
		assertEquals(2, segmentation.numDimensions());
		assertEquals(0.7, segmentation.axis(0).averageScale(0, 1), 0.0);
	}

	@Test
	public void testProbabilityMap() {
		SegmentationTool tool = new SegmentationTool();
		tool.setUseGpu(false);
		tool.openModel(fullPath("/leaf.classifier"));
		ImgPlus<FloatType> probabilityMap = tool.probabilityMap(image);
		ImgPlus<?> expectedProbabilityMap = Cast.unchecked(openImage("/leaf_probability_map.tif"));
		assertImageEqualsRealType(Cast.unchecked(expectedProbabilityMap), probabilityMap, 0.0001);
	}

	private ImgPlus<?> openImage(String file) {
		try {
			return SingletonContext.getInstance().service(DatasetIOService.class).open(fullPath(file))
				.getImgPlus();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String fullPath(String name) {
		return this.getClass().getResource(name).getFile();
	}
}
