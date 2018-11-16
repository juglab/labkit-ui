package net.imglib2.labkit_rest_api;

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
