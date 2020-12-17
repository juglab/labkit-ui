
package net.imglib2.labkit.v2.utils;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.inputimage.InputImage;
import org.scijava.Context;

import java.io.IOException;

public class InputImageIoUtils {

	public static InputImage open(Context context, String file) {

		try {
			DatasetIOService datasetIOService = context.service(DatasetIOService.class);
			Dataset dataset = datasetIOService.open(file);
			return new DatasetInputImage(dataset);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
