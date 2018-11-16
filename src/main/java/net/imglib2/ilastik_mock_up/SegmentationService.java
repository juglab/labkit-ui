package net.imglib2.ilastik_mock_up;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.dvid.ImageClient;
import net.imglib2.dvid.ImageRepository;
import net.imglib2.dvid.ImageId;
import net.imglib2.labkit_rest_api.SegmentationRequest;
import net.imglib2.labkit_rest_api.SegmentationResponse;
import net.imglib2.labkit_rest_api.TrainingRequest;
import net.imglib2.labkit_rest_api.TrainingResponse;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/segmentation")
public class SegmentationService
{
	@GET
	@Path("/{message}")
	public String getMsg(@PathParam("message") String msg)
	{
		return  "Message requested : " + msg;
	}

	@POST
	@Path("/train")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public TrainingResponse train(TrainingRequest request) {
		final TrainingResponse response = new TrainingResponse();
		response.setTrainingId(UUID.randomUUID().toString());
		return response;
	}

	@POST
	@Path("/segment")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public SegmentationResponse segment(SegmentationRequest request) {
		try {
			final SegmentationResponse response = new SegmentationResponse();
			final String url = request.getImageUrl();
			Img<RealType<?>> cached = (Img<RealType<?>>) ImageClient.asCachedImg(url);
			RandomAccessibleInterval<?> thresholded = segment(cached);
			ImageId id = ImageRepository.getInstance().addImage("segmentation", thresholded);
			response.setSegmentationUrl(id.getUrl());
			return response;
		} catch ( Exception e ) {
			e.printStackTrace();
			return null;
		}
	}

	public RandomAccessibleInterval<?> segment(RandomAccessibleInterval<RealType<?>> cached) {
		return Converters.convert(cached,
						(i, o) -> o.set(i.getRealDouble() > 100 ? (short) 1 : (short) 0),
						new ShortType());
	}
}
