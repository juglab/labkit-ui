
package net.imglib2.labkit.v2.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.imglib2.labkit.segmentation.Segmenter;

import java.util.Map;

/**
 * Represents a segmentation algorithm.
 */
public class SegmenterModel {

	@JsonIgnore
	private String name;

	@JsonProperty("file")
	private String segmenterFile; // could be moved to segmenter

	@JsonIgnore
	private Segmenter segmenter;

	private Map<ImageModel, SegmentationModel> results;

	public void setSegmenterFile(String segmenterFile) {
		this.segmenterFile = segmenterFile;
	}
}
