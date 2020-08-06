
package net.imglib2.labkit.multi_image;

import net.imagej.patcher.LegacyInjector;
import net.imglib2.labkit.SegmentationComponent;
import net.imglib2.labkit.models.LabeledImage;
import net.imglib2.labkit.models.LabkitProjectModel;
import net.imglib2.labkit.models.SegmentationItem;
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
		ProjectSegmentationModel segmentationModel = ProjectSegmentationModel.create(
			labkitProjectModel);
		SegmentationComponent component = new SegmentationComponent(frame, segmentationModel, false);
		component.autoContrast();
		JMenuBar menuBar = component.getMenuBar();
		JMenu projectMenu = initProjectMenu(segmentationModel, frame);
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

	private static JMenu initProjectMenu(ProjectSegmentationModel model, Component component) {
		JMenu menu = new JMenu("Project");
		JMenuItem newProjectItem = new JMenuItem("New Project...");
		newProjectItem.addActionListener(ignore -> onNewProjectClicked(component));
		menu.add(newProjectItem);
		JMenuItem openProjectItem = new JMenuItem("Open Project...");
		openProjectItem.addActionListener(ignore -> onOpenProjectClicked(component));
		menu.add(openProjectItem);
		JMenuItem saveProjectItem = new JMenuItem("Save Project");
		saveProjectItem.addActionListener(ignore -> onSaveProjectClicked(model));
		menu.add(saveProjectItem);
		return menu;
	}

	private static void onNewProjectClicked(Component component) {
		JFileChooser dialog = new JFileChooser();
		dialog.setDialogTitle("New Project - Please select an empty directory!");
		dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnValue = dialog.showOpenDialog(component);
		if (returnValue != JFileChooser.APPROVE_OPTION)
			return;
		File directory = dialog.getSelectedFile();
		if (!isEmptyDirectory(directory)) {
			JOptionPane.showMessageDialog(component, "The selected directory needs to be empty.", "Error",
				JOptionPane.ERROR_MESSAGE);
			return;
		}
		LabkitProjectModel newProject = new LabkitProjectModel(SingletonContext.getInstance(), directory
			.getPath(), new ArrayList<>());
		showProjectEditor(component, newProject);
		if (newProject.labeledImages().isEmpty()) {
			JOptionPane.showMessageDialog(component, "Error: Project contains no images.",
				"Create New Project", JOptionPane.ERROR_MESSAGE);
			return;
		}
		newProject.selectedImage().set(newProject.labeledImages().get(0));
		try {
			save(newProject);
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(component, "Error while saving:\n" + e.getMessage(),
				"Create New Project", JOptionPane.ERROR_MESSAGE);
			return;
		}
		openProject(newProject);
	}

	private static void showProjectEditor(Component component, LabkitProjectModel emptyProject) {
		LabkitProjectEditor editor = new LabkitProjectEditor(emptyProject);
		JDialog frame = new JDialog(SwingUtilities.getWindowAncestor(component),
			"New Project: Add some images.");
		frame.add(editor);
		frame.pack();
		frame.setModal(true);
		frame.setResizable(true);
		frame.setVisible(true);
	}

	private static boolean isEmptyDirectory(File file) {
		return file.isDirectory() && file.listFiles().length == 0;
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

	private static void onSaveProjectClicked(ProjectSegmentationModel model) {
		try {
			updateSegmenterFiles(model);
			save(model.projectModel());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void save(LabkitProjectModel project) throws IOException {
		LabkitProjectSerializer.save(project, new File(project.getProjectDirectory(),
			"labkit-project.yaml"));
	}

	private static void updateSegmenterFiles(ProjectSegmentationModel model) throws IOException {
		File segmentersDirectory = new File(model.projectModel().getProjectDirectory(), "segmenters");
		if (!segmentersDirectory.isDirectory())
			Files.createDirectory(segmentersDirectory.toPath());
		List<String> files = new ArrayList<>();
		for (SegmentationItem segmentationItem : model.segmenterList().segmenters().get()) {
			if (!segmentationItem.isTrained())
				continue;
			String result = saveSegmenter(segmentationItem, segmentersDirectory);
			files.add(result);
		}
		LabkitProjectModel projectModel = model.projectModel();
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
