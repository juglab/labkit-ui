package net.imglib2.labkit_rest_api.dvid;

import net.imglib2.Interval;

public interface ImageRepresentation {

	String typeSpecification();

	Interval interval();

	byte[] getBinaryData(Interval interval);
}
