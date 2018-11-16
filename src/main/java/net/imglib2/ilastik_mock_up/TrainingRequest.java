package net.imglib2.ilastik_mock_up;

import javax.json.bind.annotation.JsonbProperty;

public class TrainingRequest {

	@JsonbProperty
	private String imageUrl;

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
}
