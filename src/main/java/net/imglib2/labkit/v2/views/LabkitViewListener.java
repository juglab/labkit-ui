
package net.imglib2.labkit.v2.views;

import net.imglib2.labkit.v2.models.ImageModel;

public interface LabkitViewListener {

	void addImage(String file);

	void openProject(String file);

	void saveProject(String file);

	void changeActiveImage(ImageModel value);
}
