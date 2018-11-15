package net.imglib2.labkit_rest_api.dvid;

import net.imglib2.Interval;
import net.imglib2.labkit_rest_api.dvid.metadata.PixelType;

public interface ImageRepresentation {

	PixelType typeSpecification();

	Interval interval();

	byte[] getBinaryData(Interval interval);
}
