package net.imglib2.ilastik_mock_up;

import bdv.util.BdvFunctions;
import ij.IJ;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit_rest_api.ImageClient;
import net.imglib2.labkit_rest_api.ImageRepository;
import net.imglib2.labkit_rest_api.dvid.ImageId;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

public class ExampleClient {

	public static void run() {
		Img<?> image = ImageJFunctions.wrap(IJ.openImage("http://imagej.nih.gov/ij/images/t1-head.zip"));
		BdvFunctions.show(segmentRemote(image), "title");
	}

	public static Img<?> segmentRemote(Img<?> image) {
		final ImageRepository reposory = ImageRepository.getInstance();
		ImageId id = reposory.addImage("dummy", image);
		TrainingRequest request = new TrainingRequest();
		request.setImageUrl(id.getUrl("http://localhost:8571"));
		Client client = ClientBuilder.newClient();
		final Entity<?> entity = Entity.json(request);
		TrainingResponse response = client
				.target("http://localhost:8571/segmentation/train")
				.request(MediaType.APPLICATION_JSON)
				.post(entity, TrainingResponse.class);
		final ImageClient client1 = new ImageClient(response.getSegmentationUrl());
		return client1.createCachedImg();
	}
}
