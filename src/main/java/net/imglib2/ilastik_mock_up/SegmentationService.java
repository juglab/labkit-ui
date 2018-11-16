package net.imglib2.ilastik_mock_up;

import bdv.util.BdvFunctions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.labkit_rest_api.ImageClient;
import net.imglib2.labkit_rest_api.ImageRepository;
import net.imglib2.labkit_rest_api.dvid.ImageId;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
		try {
			final TrainingResponse response = new TrainingResponse();
			ImageClient client = new ImageClient(request.getImageUrl());
			Img<?> cached = client.createCachedImg();
			RandomAccessibleInterval<UnsignedByteType> thresholded =
					Converters.convert((RandomAccessibleInterval<RealType<?>>) cached,
					(i, o) -> { o.set(i.getRealDouble() > 100 ? 255 : 0); },
					new UnsignedByteType());
			ImageId id = ImageRepository.getInstance().addImage("segmentation", thresholded);
			response.setSegmentationUrl(id.getUrl("http://localhost:8571"));
			return response;
		} catch ( Exception e ) {
			e.printStackTrace();
			return null;
		}
	}
}
