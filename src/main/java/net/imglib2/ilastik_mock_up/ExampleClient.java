package net.imglib2.ilastik_mock_up;

import bdv.util.BdvFunctions;
import ij.IJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.dvid.ImageClient;
import net.imglib2.dvid.ImageRepository;
import net.imglib2.dvid.ImageId;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

public class ExampleClient {

	public static void run() {
		Img<?> image = ImageJFunctions.wrap(IJ.openImage("http://imagej.nih.gov/ij/images/t1-head.zip"));
		SegmentationClient client = new SegmentationClient();
		TrainingResponse trainingId = client.getTrainingId(image);
		System.out.println("trainingId: " + trainingId.getTrainingId());
		SegmentationResponse segmentationResponse = client.segment(image, trainingId);
		final String segmentationUrl = segmentationResponse.getSegmentationUrl();
		System.out.println("segmentationUrl: " + segmentationUrl);
		Img<?> segmented = ImageClient.asCachedImg(segmentationUrl);
		BdvFunctions.show(segmented, "segmented");
	}

	private static class SegmentationClient {

		private final ImageRepository reposory = ImageRepository.getInstance();

		private final Client client = ClientBuilder.newClient();

		public TrainingResponse getTrainingId(RandomAccessibleInterval<?> image) {
			ImageId id = reposory.addImage("dummy", image);
			TrainingRequest request = new TrainingRequest();
			request.setImageUrl(id.getUrl());
			return client.target("http://localhost:8571/segmentation/train")
					.request(MediaType.APPLICATION_JSON)
					.post(Entity.json(request), TrainingResponse.class);
		}

		public SegmentationResponse segment(RandomAccessibleInterval<?> image, TrainingResponse trainingResponse) {
			SegmentationRequest request = new SegmentationRequest();
			request.setTrainingId(trainingResponse.getTrainingId());
			ImageId id = reposory.addImage("image", image);
			request.setImageUrl(id.getUrl());
			return client.target("http://localhost:8571/segmentation/segment")
					.request(MediaType.APPLICATION_JSON)
					.post(Entity.json(request), SegmentationResponse.class);
		}
	}
}
