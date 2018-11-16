package net.imglib2.labkit_rest_api.dvid.metadata;

import net.imglib2.type.Type;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import javax.json.bind.annotation.JsonbProperty;

public enum PixelType {
	@JsonbProperty("uint8") UNSIGNED_BYTE(new UnsignedByteType()),
	@JsonbProperty("int8") BYTE(new ByteType()),
	@JsonbProperty("uint16") UNSIGNED_SHORT(new UnsignedShortType()),
	@JsonbProperty("int16") SHORT(new ShortType()),
	@JsonbProperty("uint32") UNSIGNED_INT(new UnsignedIntType()),
	@JsonbProperty("int32") INT(new IntType()),
	@JsonbProperty("uint64") UNSIGNED_LONG(new UnsignedLongType()),
	@JsonbProperty("int64") LONG(new LongType()),
	@JsonbProperty("float32") FLOAT(new FloatType()),
	@JsonbProperty("float64") DOUBLE(new DoubleType());

	private final Type<?> instance;

	PixelType(Type<?> instance) {
		this.instance = instance;
	}

	public Type<?> getType() {
		return instance;
	}

	public static PixelType valueOf(Type<?> type) {
		for(PixelType value : PixelType.values())
			if(value.getType().getClass().isInstance(type))
				return value;
		throw new IllegalArgumentException("Pixel type (" + type.getClass().getName() + ") cannot be represented as DVID image.");
	}
}
