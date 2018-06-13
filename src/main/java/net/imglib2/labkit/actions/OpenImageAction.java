
package net.imglib2.labkit.actions;

import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.MainFrame;

/**
 * @author Matthias Arzt
 */
public class OpenImageAction extends AbstractFileIoAcion {

	public OpenImageAction(Extensible extensible) {
		super(extensible, AbstractFileIoAcion.TIFF_FILTER);
		initOpenAction("Open Image ...", "openImage", filename -> MainFrame.open(
			extensible.context(), filename, false), "");
		initOpenAction("Open Image (Time Series) ...", "openTimeSeries",
			filename -> MainFrame.open(extensible.context(), filename, true), "");
	}
}
