package net.imglib2.atlas.actions;

import net.imglib2.atlas.MainFrame;

/**
 * @author Matthias Arzt
 */
public class OpenImageAction extends AbstractFileIoAcion {

	public OpenImageAction(MainFrame.Extensible extensible) {
		super(extensible, AbstractFileIoAcion.TIFF_FILTER);
		initOpenAction("Open Image ...", "openImage", filename -> MainFrame.open(extensible.context(), filename), "");
	}
}
