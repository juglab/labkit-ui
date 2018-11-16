package net.imglib2.dvid;

import net.imglib2.Interval;
import net.imglib2.dvid.metadata.PixelType;

public interface ImageRepresentation {

	PixelType typeSpecification();

	Interval interval();

	byte[] getBinaryData(Interval interval);
}
