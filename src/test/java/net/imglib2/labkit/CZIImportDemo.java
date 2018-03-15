package net.imglib2.labkit;

import net.imagej.ImageJ;
import net.imglib2.labkit.plugin.LabkitImportPlugin;
import net.imglib2.labkit.plugin.LabkitPlugin;

import java.io.File;
import java.io.IOException;

/**
 * Created by random on 13.03.18.
 */
public class CZIImportDemo {

    public static void main(String... args) throws IOException {
        ImageJ imageJ = new ImageJ();
        imageJ.ui().showUI();
        String filename = "/home/random/Development/imagej/project/labkit/data/Lung Images/2017_11_30__0034.czi";
        File file = new File(filename);

        imageJ.command().run(LabkitImportPlugin.class, true, "file", file);
    }
}
