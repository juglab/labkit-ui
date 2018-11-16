package net.imglib2.labkit_rest_api;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.dvid.ImageClient;
import net.imglib2.dvid.ImageId;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.dvid.ImageRepository;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;
import org.scijava.Context;

import javax.swing.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class RestSegmenter implements Segmenter {

	private Notifier<Runnable> listeners = new Notifier<>();
	private final Client client = ClientBuilder.newClient();
	private final ImageRepository reposory = ImageRepository.getInstance();
	private final Map<RandomAccessibleInterval<?>, ImageClient> imageClients = new HashMap<>();
	private String trainingId;

	public RestSegmenter(Context context, InputImage inputImage) {

	}

	public RestSegmenter(Context context, Object segmenter) {
		throw new RuntimeException("TODO, remove the usage of this constructor. It makes no sense.");
	}

	@Override
	public void editSettings(JFrame dialogParent) {

	}

	@Override
	public void train(List<Pair<? extends RandomAccessibleInterval<?>, ? extends Labeling>> data) {
		RandomAccessibleInterval<?> image = data.get(0).getA();
		ImageId id = reposory.addImage("dummy", image);
		TrainingRequest request = new TrainingRequest();
		request.setImageUrl(id.getUrl());
		TrainingResponse response = client.target("http://localhost:8571/segmentation/train")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.json(request), TrainingResponse.class);
		trainingId = response.getTrainingId();
		imageClients.clear();
		listeners.forEach(Runnable::run);
	}

	@Override
	public void segment(RandomAccessibleInterval<?> image, RandomAccessibleInterval<? extends IntegerType<?>> output) {
		ImageClient client = imageClients.get(image);
		if(client == null) {
			client = getSegmentationUrl(image);
			imageClients.put(image, client);
		}
		client.copyChunk(output);
	}

	public ImageClient getSegmentationUrl(RandomAccessibleInterval<?> image) {
		SegmentationRequest request = new SegmentationRequest();
		request.setTrainingId(trainingId);
		ImageId id = reposory.addImage("image", image);
		request.setImageUrl(id.getUrl());
		SegmentationResponse response = client.target("http://localhost:8571/segmentation/segment")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.json(request), SegmentationResponse.class);
		return new ImageClient(response.getSegmentationUrl());
	}

	@Override
	public void predict(RandomAccessibleInterval<?> image, RandomAccessibleInterval<? extends RealType<?>> output) {

	}

	@Override
	public boolean isTrained() {
		return true;
	}

	@Override
	public void saveModel(String path) throws Exception {

	}

	@Override
	public void openModel(String path) throws Exception {

	}

	@Override
	public Notifier<Runnable> trainingCompletedListeners() {
		return listeners;
	}

	@Override
	public List<String> classNames() {
		return Arrays.asList("background", "foreground");
	}
}
