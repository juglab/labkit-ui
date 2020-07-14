
package net.imglib2.labkit.multi_image;

import net.imagej.patcher.LegacyInjector;
import net.imglib2.labkit.SegmentationComponent;
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.labkit.models.LabeledImage;
import net.imglib2.labkit.models.LabkitProjectModel;
import net.imglib2.trainable_segmentation.utils.SingletonContext;
import weka.gui.ExtensionFileFilter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiImageDemo {

	static {
		LegacyInjector.preinit();
	}

	private static List<LabeledImage> files = Stream.of(
		"/home/arzt/tmp/labkit-project/phase1.tif",
		"/home/arzt/tmp/labkit-project/phase2.tif",
		"/home/arzt/tmp/labkit-project/phase3.tif",
		"/home/arzt/tmp/labkit-project/phase4.tif")
		.map(LabeledImage::new)
		.collect(Collectors.toList());

	public static void main(String... args) {
		List<LabeledImage> imageFiles = files;
		LabkitProjectModel labkitProjectModel = new LabkitProjectModel(
			SingletonContext.getInstance(), imageFiles);
		openProject(labkitProjectModel);
	}

	private static void openProject(LabkitProjectModel labkitProjectModel) {
		JFrame frame = new JFrame("Labkit Project");
		DefaultSegmentationModel segmentationModel = ProjectSegmentationModel.init(labkitProjectModel);
		SegmentationComponent component = new SegmentationComponent(frame, segmentationModel, false);
		component.autoContrast();
		JMenuBar menuBar = component.getMenuBar();
		JMenu projectMenu = initProjectMenu(labkitProjectModel, frame);
		menuBar.add(projectMenu);
		frame.setJMenuBar(menuBar);
		JPanel panel = new LabkitProjectView(labkitProjectModel);
		labkitProjectModel.selectedImage().notifier().add(component::autoContrast);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, component.getComponent(),
			panel);
		splitPane.setResizeWeight(1);
		splitPane.setOneTouchExpandable(true);
		frame.add(splitPane);
		frame.pack();
		frame.setVisible(true);
	}

	private static JMenu initProjectMenu(LabkitProjectModel labkitProjectModel, Component component) {
		JMenu menu = new JMenu("Project");
		JMenuItem openProjectItem = new JMenuItem("Open Project");
		openProjectItem.addActionListener(ignore -> onOpenProjectClicked(component));
		menu.add(openProjectItem);
		JMenuItem saveProjectItem = new JMenuItem("Save Project");
		saveProjectItem.addActionListener(ignore -> onSaveProjectClicked(labkitProjectModel,
			component));
		menu.add(saveProjectItem);
		return menu;
	}

	private static void onOpenProjectClicked(Component component) {
		JFileChooser dialog = new JFileChooser();
		dialog.setFileFilter(new ExtensionFileFilter("yaml", "Labkit Project (*.yaml)"));
		int returnValue = dialog.showOpenDialog(component);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File file = dialog.getSelectedFile();
			try {
				openProject(LabkitProjectSerializer.open(SingletonContext.getInstance(), file));
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void onSaveProjectClicked(LabkitProjectModel project, Component component) {
		JFileChooser dialog = new JFileChooser();
		dialog.setFileFilter(new ExtensionFileFilter("yaml", "Labkit Project (*.yaml)"));
		int returnValue = dialog.showSaveDialog(component);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File file = dialog.getSelectedFile();
			try {
				LabkitProjectSerializer.save(project, file);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
