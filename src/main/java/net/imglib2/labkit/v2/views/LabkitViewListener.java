
package net.imglib2.labkit.v2.views;

import net.imglib2.labkit.v2.models.ImageModel;

public interface LabkitViewListener {

	void onAddImage();

	void onOpenProject();

	void onSaveProject();

	void onChangeActiveImage(ImageModel value);
}
