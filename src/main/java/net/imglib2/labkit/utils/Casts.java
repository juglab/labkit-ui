
package net.imglib2.labkit.utils;

public class Casts {

	public static <T> T unchecked(Object input) {
		@SuppressWarnings("unchecked")
		T result = (T) input;
		return result;
	}
}
