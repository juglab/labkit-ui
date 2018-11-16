package net.imglib2.labkit_rest_api;

import javax.json.bind.annotation.JsonbProperty;

public class TrainingResponse {

	@JsonbProperty
	private String trainingId;

	public String getTrainingId() {
		return trainingId;
	}

	public void setTrainingId(String trainingId) {
		this.trainingId = trainingId;
	}
}
