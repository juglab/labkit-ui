
package net.imglib2.labkit.v2.views;

import net.imglib2.labkit.v2.models.ImageModel;

import java.util.List;

public interface LabkitViewListener {

	void addImage(String file);

	void openProject(String file);

	void saveProject();

	void saveProjectAs(String file);

	void changeActiveImage(ImageModel value);

	void removeImages(List<ImageModel> images);
}
