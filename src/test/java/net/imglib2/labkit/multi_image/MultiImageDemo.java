
package net.imglib2.labkit.multi_image;

import net.imagej.patcher.LegacyInjector;
import net.imglib2.labkit.SegmentationComponent;
import net.imglib2.labkit.models.LabeledImage;
import net.imglib2.labkit.models.LabkitProjectModel;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmenterListModel;
import net.imglib2.trainable_segmentation.utils.SingletonContext;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
			SingletonContext.getInstance(), "/home/arzt/tmp/labkit-project", imageFiles);
		openProject(labkitProjectModel);
	}

	private static void openProject(LabkitProjectModel labkitProjectModel) {
		JFrame frame = new JFrame("Labkit Project");
		JPanel panel = new LabkitProjectView(labkitProjectModel);
		JPanel workspace = new JPanel();
		workspace.setLayout(new BorderLayout());
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, workspace, panel);
		// TODO the next line is realy hacky
		SegmenterListModel segmenterListModel = new ProjectSegmentationModel(labkitProjectModel, null)
			.segmenterList();
		JMenu projectMenu = initProjectMenu(frame, labkitProjectModel, segmenterListModel);
		labkitProjectModel.selectedImage().notifier().add(() -> {
			workspace.removeAll();
			frame.setJMenuBar(null);
			LabeledImage labeledImage = labkitProjectModel.selectedImage().get();
			if (labeledImage != null) {
				ProjectSegmentationModel segementationModel = new ProjectSegmentationModel(
					labkitProjectModel, segmenterListModel);
				SegmentationComponent component = new SegmentationComponent(frame, segementationModel,
					false);
				component.autoContrast();
				workspace.add(component.getComponent());
				JMenuBar menuBar = component.getMenuBar();
				menuBar.add(projectMenu);
				frame.setJMenuBar(menuBar);
				menuBar.updateUI();
			}
			workspace.revalidate();
			workspace.repaint();
		});
		splitPane.setResizeWeight(1);
		splitPane.setOneTouchExpandable(true);
		frame.add(splitPane);
		frame.pack();
		frame.setVisible(true);
	}

	private static JMenu initProjectMenu(Component component, LabkitProjectModel projectModel,
		SegmenterListModel segmenterListModel)
	{
		JMenu menu = new JMenu("Project");
		JMenuItem newProjectItem = new JMenuItem("New Project...");
		newProjectItem.addActionListener(ignore -> onNewProjectClicked(component));
		menu.add(newProjectItem);
		JMenuItem openProjectItem = new JMenuItem("Open Project...");
		openProjectItem.addActionListener(ignore -> onOpenProjectClicked(component));
		menu.add(openProjectItem);
		JMenuItem saveProjectItem = new JMenuItem("Save Project");
		saveProjectItem.addActionListener(ignore -> {
			onSaveProjectClicked(projectModel, segmenterListModel);
		});
		menu.add(saveProjectItem);
		return menu;
	}

	private static void onNewProjectClicked(Component component) {
		LabkitProjectModel newProject = NewProjectDialog.show(component);
		if (newProject == null) return;
		openProject(newProject);
	}

	private static void onOpenProjectClicked(Component component) {
		JFileChooser dialog = new JFileChooser();
		dialog.setFileFilter(new LabkitProjectFileFilter());
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

	private static void onSaveProjectClicked(LabkitProjectModel projectModel,
		SegmenterListModel segmenterListModel)
	{
		try {
			updateSegmenterFiles(projectModel, segmenterListModel);
			LabkitProjectSerializer.save(projectModel, new File(projectModel.getProjectDirectory(),
				"labkit-project.yaml"));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void updateSegmenterFiles(LabkitProjectModel projectModel,
		SegmenterListModel segmenterListModel) throws IOException
	{
		File segmentersDirectory = new File(projectModel.getProjectDirectory(), "segmenters");
		if (!segmentersDirectory.isDirectory())
			Files.createDirectory(segmentersDirectory.toPath());
		List<String> files = new ArrayList<>();
		for (SegmentationItem segmentationItem : segmenterListModel.segmenters().get()) {
			if (!segmentationItem.isTrained())
				continue;
			String result = saveSegmenter(segmentationItem, segmentersDirectory);
			files.add(result);
		}
		List<String> oldFiles = projectModel.segmenterFiles();
		removeObsoleteFiles(files, oldFiles);
		projectModel.segmenterFiles().clear();
		projectModel.segmenterFiles().addAll(files);
	}

	private static String saveSegmenter(SegmentationItem segmentationItem, File directory) {
		String fileName = segmentationItem.getFileName();
		String result;
		if (!segmentationItem.isModified() && fileName != null && fileExists(fileName) &&
			fileIsInDirectory(fileName, directory))
		{
			result = segmentationItem.getFileName();
		}
		else {
			File file = newFileName(directory);
			segmentationItem.saveModel(file.getPath());
			result = file.getPath();
		}
		return result;
	}

	private static void removeObsoleteFiles(Collection<String> updatedFiles,
		Collection<String> oldFiles)
	{
		Set<String> obsoleteFiles = new HashSet<>(oldFiles);
		obsoleteFiles.removeAll(updatedFiles);
		obsoleteFiles.forEach(file -> {
			try {
				Files.deleteIfExists(Paths.get(file));
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private static boolean fileIsInDirectory(String fileName, File segmentersDirectory) {
		return new File(fileName).getParentFile().equals(segmentersDirectory);
	}

	private static boolean fileExists(String fileName) {
		return new File(fileName).exists();
	}

	private static File newFileName(File directory) {
		for (int i = 1;; i++) {
			File segmenterFile = new File(directory, "segmenter-" + i + ".classifier");
			if (!segmenterFile.exists())
				return segmenterFile;
		}
	}
}
