package net.imglib2.labkit.imageserver;

public class DvidImageMetadata {
	private final long[] sizes;

	public DvidImageMetadata(long[] sizes) {
		this.sizes = sizes;
	}

	public long[] getSizes() {
		return sizes;
	}
}
