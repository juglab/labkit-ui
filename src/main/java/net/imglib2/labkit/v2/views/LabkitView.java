
package net.imglib2.labkit.v2.views;

import net.imglib2.labkit.LabelingComponent;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.v2.models.ImageModel;
import net.imglib2.labkit.v2.models.LabkitModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LabkitView extends JFrame {

	// Model

	private final LabkitModel model;

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

	public LabkitView(LabkitModel model) {
		this.model = model;
		setJMenuBar(menuBar);
		initializeMenuBar();
		add(activeImageLabel, BorderLayout.PAGE_START);
		workspacePanel.setLayout(new BorderLayout());
		JPanel rightPanel = initRightPanel();
		add(initSplitPanel(workspacePanel, rightPanel));
		imageList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				ImageModel value = model.getImageModels().get(imageList.getSelectedIndex());
				listeners.forEach(listener -> listener.onChangeActiveImage(value));
			}
		});
		pack();
	}

	// Initialization

	private void initializeMenuBar() {
		JMenu projectMenu = new JMenu("Project");
		projectMenu.add(createMenuItem("Open Project", LabkitViewListener::onOpenProject));
		projectMenu.add(createMenuItem("Save Project", LabkitViewListener::onSaveProject));
		projectMenu.add(new JSeparator());
		projectMenu.add(new JMenuItem("Close"));
		menuBar.add(projectMenu);
	}

	private JMenuItem createMenuItem(String title, Consumer<LabkitViewListener> action) {
		JMenuItem menuItem = new JMenuItem(title);
		menuItem.addActionListener(l -> listeners.forEach(action));
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
		addImageButton.addActionListener(e -> listeners.forEach(LabkitViewListener::onAddImage));
		panel.add(addImageButton);
		panel.setPreferredSize(new Dimension(200, panel.getPreferredSize().height));
		return panel;
	}

	// Updater

	public void updateImageList() {
		listAdapter.triggerUpdate();
	}

	public void updateActiveImage() {
		ImageModel activeImageModel = model.getActiveImageModel();
		String text = activeImageModel.getName();
		if (activeLabelingComponent != null) {
			workspacePanel.remove(activeLabelingComponent);
			activeLabelingComponent.close();
		}
		ImageLabelingModel ilm = new ImageLabelingModel(activeImageModel.getImage());
		ilm.labeling().set(activeImageModel.getLabeling());
		activeLabelingComponent = new LabelingComponent(this, ilm);
		workspacePanel.add(activeLabelingComponent);
		activeImageLabel.setText(text);
	}

	// Listeners

	public void addListerner(LabkitViewListener listener) {
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
		model.getImageModels().add(ImageModel.createForImageFile("a.tif"));
		model.getImageModels().add(ImageModel.createForImageFile("b.tif"));
		new LabkitView(model).setVisible(true);
	}
}
