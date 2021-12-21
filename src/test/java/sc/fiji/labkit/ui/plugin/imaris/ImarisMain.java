package sc.fiji.labkit.ui.plugin.imaris;

import net.imagej.ImageJ;
import net.imagej.patcher.LegacyInjector;
import net.imglib2.labkit.imaris.LabkitImarisPlugin;

public class ImarisMain {
	static {
		LegacyInjector.preinit();
	}

	public static void main(String[] args) {
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();

        // (Plugins > Segmentation > Labkit > Image from Imaris)
        ij.command().run( LabkitImarisPlugin.class, true);
//		LabkitImarisPlugin.imageFromImaris( "StoreClassifiersPath=\"/Users/pietzsch/Desktop/classifiers\"" );
//		LabkitImarisPlugin.imageFromImaris( "Classifier=\"/Users/pietzsch/Desktop/classifiers/labkit6698308194340155162.classifier\" -Headless \"true\"" );
	}
}
