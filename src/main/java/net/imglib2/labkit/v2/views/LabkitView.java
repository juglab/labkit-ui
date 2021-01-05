
package net.imglib2.labkit.v2.views;

import net.imglib2.labkit.LabelingComponent;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.project.LabkitProjectFileFilter;
import net.imglib2.labkit.v2.models.ImageModel;
import net.imglib2.labkit.v2.models.LabkitModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LabkitView extends JFrame {

	// Model

	private LabkitModel model;

	// UI Components

	private final JMenuBar menuBar = new JMenuBar();

	private final ListAdapter listAdapter = new ListAdapter();

	private final JList<String> imageList = new JList<>(listAdapter);

	private final JLabel activeImageLabel = new JLabel("-");

	private final JPanel workspacePanel = initWorkspace();

	private LabelingComponent activeLabelingComponent;

	// Listeners

	private final List<LabkitViewListener> listeners = new CopyOnWriteArrayList<>();

	// Constructor

	public LabkitView() {
		this.model = new LabkitModel();
		setJMenuBar(menuBar);
		initializeMenuBar();
		add(activeImageLabel, BorderLayout.PAGE_START);
		workspacePanel.setLayout(new BorderLayout());
		JPanel rightPanel = initRightPanel();
		add(initSplitPanel(workspacePanel, rightPanel));
		imageList.addListSelectionListener(this::onListSelectionChanged);
		pack();
	}

	public void setModel(LabkitModel model) {
		this.model = model;
	}

	// Initialization

	private void initializeMenuBar() {
		JMenu projectMenu = new JMenu("Project");
		projectMenu.add(createMenuItem("Open Project", this::onOpenProject));
		projectMenu.add(createMenuItem("Save Project", this::onSaveProject));
		projectMenu.add(createMenuItem("Save Project As ...", this::onSaveProjectAs));
		projectMenu.add(new JSeparator());
		projectMenu.add(new JMenuItem("Close"));
		menuBar.add(projectMenu);
	}

	private void onOpenProject() {
		JFileChooser dialog = new JFileChooser();
		dialog.setFileFilter(new LabkitProjectFileFilter());
		int result = dialog.showOpenDialog(this);
		if (result != JFileChooser.APPROVE_OPTION)
			return;
		String file = dialog.getSelectedFile().getAbsolutePath();
		listeners.forEach(listener -> listener.openProject(file));
	}

	private void onSaveProject() {
		listeners.forEach(LabkitViewListener::saveProject);
	}

	private void onSaveProjectAs() {
		JFileChooser dialog = new JFileChooser();
		dialog.setFileFilter(new LabkitProjectFileFilter());
		int result = dialog.showSaveDialog(this);
		if (result != JFileChooser.APPROVE_OPTION)
			return;
		String file = dialog.getSelectedFile().getAbsolutePath();
		listeners.forEach(listener -> listener.saveProjectAs(file));
	}

	private JMenuItem createMenuItem(String title, Runnable action) {
		JMenuItem menuItem = new JMenuItem(title);
		menuItem.addActionListener(l -> action.run());
		return menuItem;
	}

	private JSplitPane initSplitPanel(JPanel workspace, JPanel rightPanel) {
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, workspace, rightPanel);
		splitPane.setResizeWeight(1);
		splitPane.setOneTouchExpandable(true);
		return splitPane;
	}

	private JPanel initWorkspace() {
		JPanel workspace = new JPanel();
		workspace.setBackground(Color.DARK_GRAY);
		workspace.setPreferredSize(new Dimension(800, 500));
		return workspace;
	}

	private JPanel initRightPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("", "[grow]", "[][grow][]"));
		panel.add(new JLabel("Images"), "wrap");
		panel.add(new JScrollPane(imageList), "grow, wrap");
		JButton addImageButton = new JButton("add");
		addImageButton.addActionListener(ignore -> onAddImage());
		panel.add(addImageButton, "split 2");
		JButton removeImagesButton = new JButton("remove");
		removeImagesButton.addActionListener(ignore -> onRemoveImages());
		panel.add(removeImagesButton);
		panel.setPreferredSize(new Dimension(200, panel.getPreferredSize().height));
		return panel;
	}

	private void onAddImage() {
		JFileChooser dialog = new JFileChooser();
		int result = dialog.showOpenDialog(this);
		if (result != JFileChooser.APPROVE_OPTION)
			return;
		String file = dialog.getSelectedFile().getAbsolutePath();
		listeners.forEach(listener -> listener.addImage(file));
	}

	private void onRemoveImages() {
		int[] selected = imageList.getSelectedIndices();
		List<ImageModel> images = model.getImageModels();
		List<ImageModel> selectedImages = IntStream.of(selected).mapToObj(images::get).collect(
			Collectors.toList());
		listeners.forEach(listener -> listener.removeImages(selectedImages));
	}

	// Event Listeners

	private void onListSelectionChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			int index = imageList.getSelectedIndex();
			List<ImageModel> imageModels = model.getImageModels();
			if (index < 0 || index >= imageModels.size())
				return;
			ImageModel value = imageModels.get(index);
			listeners.forEach(listener -> listener.changeActiveImage(value));
		}
	}

	// Updater

	public void updateImageList() {
		listAdapter.triggerUpdate();
	}

	public void updateActiveImage() {
		updateActiveImageLabel();
		updateWorkspace();
		updateSelection();
	}

	private void updateActiveImageLabel() {
		ImageModel activeImageModel = model.getActiveImageModel();
		activeImageLabel.setText(activeImageModel != null ? activeImageModel.getName() : "-");
	}

	private void updateWorkspace() {
		ImageModel activeImageModel = model.getActiveImageModel();
		if (activeLabelingComponent != null) {
			workspacePanel.remove(activeLabelingComponent);
			activeLabelingComponent.close();
			activeLabelingComponent = null;
		}
		if (activeImageModel == null) {
			activeImageLabel.setText("-");
		}
		else {
			ImageLabelingModel ilm = new ImageLabelingModel(activeImageModel.getImage());
			ilm.labeling().set(activeImageModel.getLabeling());
			ilm.labeling().notifier().addListener(() -> activeImageModel.setLabelingModified(true));
			ilm.dataChangedNotifier().addListener(ignore -> activeImageModel.setLabelingModified(true));
			activeLabelingComponent = new LabelingComponent(this, ilm);
			workspacePanel.add(activeLabelingComponent);
			activeImageLabel.setText(activeImageModel.getName());
		}
		workspacePanel.revalidate();
		workspacePanel.repaint();
	}

	private void updateSelection() {
		ImageModel activeImageModel = model.getActiveImageModel();
		int index = model.getImageModels().indexOf(activeImageModel);
		if (imageList.getSelectedIndex() != index)
			imageList.setSelectedIndex(index);
	}

	// Listeners

	public void addListener(LabkitViewListener listener) {
		listeners.add(listener);
	}

	public void removeListener(LabkitViewListener listener) {
		listeners.remove(listener);
	}

	// Data visualization

	private class ListAdapter implements ListModel<String> {

		private final List<ListDataListener> listeners = new CopyOnWriteArrayList<>();

		private void triggerUpdate() {
			ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize());
			listeners.forEach(l -> l.contentsChanged(e));
		}

		@Override
		public int getSize() {
			return model.getImageModels().size();
		}

		@Override
		public String getElementAt(int index) {
			return model.getImageModels().get(index).getName();
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			listeners.add(l);
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			listeners.remove(l);
		}
	}

	// demo

	public static void main(String... args) {
		LabkitModel model = new LabkitModel();
		LabkitView view = new LabkitView();
		view.setModel(model);
		view.updateImageList();
		view.setVisible(true);
	}
}
