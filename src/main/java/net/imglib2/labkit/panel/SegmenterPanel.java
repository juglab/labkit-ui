
package net.imglib2.labkit.panel;

import net.imglib2.labkit.DefaultExtensible;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmenterListModel;
import net.imglib2.labkit.segmentation.AddSegmenterPanel;
import net.imglib2.labkit.segmentation.SegmentationPlugin;
import net.imglib2.labkit.segmentation.SegmentationPluginService;
import net.imglib2.labkit.segmentation.TrainClassifier;
import net.imglib2.labkit.utils.ParallelUtils;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class SegmenterPanel extends JPanel {

	private final SegmenterListModel segmentationModel;

	private final ComponentList<SegmentationItem, JPanel> list = new ComponentList<>();

	private final Function<Supplier<SegmentationItem>, JPopupMenu> menuFactory;

	private final JPanel listPanel = new JPanel();

	private final AddSegmenterPanel addSegmenterPanel;

	private JButton addSegmenterButton;

	private final Runnable updateList = this::updateList;

	public SegmenterPanel(
		SegmenterListModel segmentationModel,
		Function<Supplier<SegmentationItem>, JPopupMenu> menuFactory)
	{
		this.segmentationModel = segmentationModel;
		this.menuFactory = menuFactory;
		addSegmenterPanel = new AddSegmenterPanel(segmentationModel);
		setLayout(new BorderLayout());
		add(addSegmenterPanel);
		listPanel.setLayout(new MigLayout("insets 0, gap 0", "[grow]", "[grow][]"));
		listPanel.add(initList(), "grow, wrap");
		listPanel.add(initBottomPanel(), "grow");
	}

	public static JPanel newFramedSegmeterPanel(
		SegmenterListModel segmentationModel,
		DefaultExtensible extensible)
	{
		SegmenterPanel segmenterPanel = new SegmenterPanel(
			segmentationModel, item -> extensible.createPopupMenu(
				SegmentationItem.SEGMENTER_MENU, item));
		return GuiUtils.createCheckboxGroupedPanel(segmentationModel
			.segmentationVisibility(), "Segmentation", segmenterPanel);
	}

	private JComponent initBottomPanel() {
		JPanel result = new JPanel();
		result.setBackground(UIManager.getColor("List.background"));
		result.setLayout(new MigLayout("insets 4pt, gap 4pt"));
		result.add(initAddButton());
		return result;
	}

	private JButton initAddButton() {
		addSegmenterButton = GuiUtils.createActionIconButton("Add classifier", new RunnableAction(
			"Add classifier", this::showAddSegmenterPopupMenu), "add.png");
		return addSegmenterButton;
	}

	private void showAddSegmenterPopupMenu() {
		SegmentationPluginService pluginService = segmentationModel.context().service(
			SegmentationPluginService.class);
		List<SegmentationPlugin> plugins = pluginService.getSegmentationPlugins();
		JPopupMenu menu = new JPopupMenu();
		for (SegmentationPlugin plugin : plugins) {
			JMenuItem menuItem = new JMenuItem(plugin.getTitle());
			menuItem.addActionListener(ignore -> addSegmenter(plugin));
			menu.add(menuItem);
		}
		menu.show(addSegmenterButton, 0, addSegmenterButton.getHeight());
	}

	private void addSegmenter(SegmentationPlugin plugin) {
		segmentationModel.addSegmenter(plugin);
	}

	private void updateList() {
		List<SegmentationItem> segmenters = segmentationModel.segmenters().get();
		updateVisiblePanel(segmenters.isEmpty());
		list.clear();
		segmenters.forEach(item -> list.add(item, new EntryPanel(item)));
		list.setSelected(segmentationModel.selectedSegmenter().get());
		revalidate();
		repaint();
	}

	private void updateVisiblePanel(boolean empty) {
		if (empty != (getComponent(0) == addSegmenterPanel)) {
			removeAll();
			add(empty ? addSegmenterPanel : listPanel);
		}
	}

	private class EntryPanel extends JPanel {

		private final SegmentationItem item;
		private final RunnableAction settingsAction = GuiUtils.createAction(
			"Settings ...", this::showSettings, "gear.png");
		private final RunnableAction trainAction = GuiUtils.createAction("Train",
			this::runTraining, "run.png");

		private EntryPanel(SegmentationItem item) {
			this.item = item;
			setLayout(new MigLayout("inset 4, gap 4, fillx"));
			JLabel label = new JLabel(item.toString());
			add(label, "push, grow, width 0:0:1000");
			add(initPopupMenuButton());
			add(GuiUtils.createIconButton(settingsAction));
			add(GuiUtils.createIconButton(trainAction));
		}

		private JButton initPopupMenuButton() {
			JPopupMenu menu = initPopupMenu();
			JButton button = new BasicArrowButton(SwingConstants.SOUTH);
			button.addActionListener(actionEvent -> {
				menu.show(button, 0, button.getHeight());
			});
			return button;
		}

		private JPopupMenu initPopupMenu() {
			return menuFactory.apply(() -> item);
		}

		private void showSettings() {
			item.editSettings(null, segmentationModel.trainingData().get());
		}

		private void runTraining() {
			ParallelUtils.runInOtherThread(() -> {
				segmentationModel.selectedSegmenter().set(item);
				TrainClassifier.trainSelectedSegmenter(segmentationModel);
			});
		}

	}

	private JComponent initList() {
		updateList();
		list.listeners().addListener(this::userChangedSelection);
		segmentationModel.segmenters().notifier().addWeakListener(updateList);
		JComponent component = list.getComponent();
		component.setBorder(BorderFactory.createEmptyBorder());
		return component;
	}

	private void userChangedSelection() {
		SegmentationItem selectedValue = list.getSelected();
		if (selectedValue != null)
			segmentationModel.selectedSegmenter().set(selectedValue);
	}
}
