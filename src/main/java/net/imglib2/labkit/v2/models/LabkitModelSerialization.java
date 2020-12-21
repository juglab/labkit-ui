
package net.imglib2.labkit.v2.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

/**
 * Allows to open and save a {@link LabkitModel}.
 */
public class LabkitModelSerialization {

	public static void save(LabkitModel model, String file) {
		try {
			ObjectMapper om = new ObjectMapper(new YAMLFactory());
			om.writeValue(new File(file), model);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static LabkitModel open(String file) {
		try {
			ObjectMapper om = new ObjectMapper(new YAMLFactory());
			return om.readValue(new File(file), LabkitModel.class);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
