package net.imglib2.ilastik_mock_up;

import javax.json.bind.annotation.JsonbProperty;

public class TrainingResponse {

	@JsonbProperty
	private String segmentationUrl;

	public String getSegmentationUrl() {
		return segmentationUrl;
	}

	public void setSegmentationUrl(String segmentationUrl) {
		this.segmentationUrl = segmentationUrl;
	}
}
