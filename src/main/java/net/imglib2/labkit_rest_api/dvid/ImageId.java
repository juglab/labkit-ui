package net.imglib2.labkit_rest_api.dvid;

import net.imglib2.labkit_rest_api.ImageRepository;

public class ImageId {

	private static final ImageRepository repository = ImageRepository.getInstance();
	private final String uuid;
	private final String dataName;

	@Deprecated
	public ImageId() {
		// don't use
		uuid = null;
		dataName = null;
	}

	public ImageId(String uuid, String dataName) {
		this.uuid = uuid;
		this.dataName = dataName;
	}

	public String getUuid() {
		return uuid;
	}

	public String getDataName() {
		return dataName;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;

		ImageId imageId = (ImageId) obj;

		if (!uuid.equals(imageId.uuid)) return false;
		return dataName.equals(imageId.dataName);
	}

	@Override
	public int hashCode() {
		int result = uuid.hashCode();
		result = 31 * result + dataName.hashCode();
		return result;
	}

	@Override
	public String toString()
	{
		return uuid + "/" + dataName;
	}

	public String getUrl() {
		return repository.getUrl() + "/node/" + toString();
	}
}
