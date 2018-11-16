package net.imglib2.ilastik_mock_up;

import javax.json.bind.annotation.JsonbProperty;

public class SegmentationRequest {

	@JsonbProperty
	private String trainingId;

	@JsonbProperty
	private String imageUrl;

	public String getTrainingId() {
		return trainingId;
	}

	public void setTrainingId(String trainingId) {
		this.trainingId = trainingId;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
}
