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

package sc.fiji.labkit.ui;

import ij.ImagePlus;
import io.scif.img.ImgSaver;
import net.imagej.ImgPlus;
import net.imglib2.img.VirtualStackAdapter;
import sc.fiji.labkit.ui.segmentation.Segmenter;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import bdv.export.ProgressWriter;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;

import java.io.File;

/**
 * @deprecated Use {@link sc.fiji.labkit.ui.segmentation.SegmentationTool}
 *             instead.
 *             <p>
 *             Helper class for segmenting multiple image files.
 * @author Matthias Arzt
 */
@Deprecated
public class BatchSegmenter {

	private final ImgSaver saver = new ImgSaver();
	private final Segmenter segmenter;
	private final ProgressWriter progressWriter;

	public BatchSegmenter(Segmenter segmenter, ProgressWriter progressWriter) {
		this.segmenter = segmenter;
		this.progressWriter = progressWriter;
	}

	public void segment(File inputFile, File outputFile) {
		ImgPlus<?> img = VirtualStackAdapter.wrap(new ImagePlus(inputFile.getAbsolutePath()));
		Img<UnsignedByteType> result = ArrayImgs.unsignedBytes(Intervals.dimensionsAsLongArray(img));
		segmenter.segment(img, result);
		saver.saveImg(outputFile.getAbsolutePath(), result);
	}

}
