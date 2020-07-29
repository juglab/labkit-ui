
package net.imglib2.labkit.panel;

import net.imglib2.labkit.DefaultExtensible;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmenterListModel;
import net.imglib2.labkit.segmentation.SegmentationPlugin;
import net.imglib2.labkit.segmentation.SegmentationPluginService;
import net.imglib2.labkit.segmentation.TrainClassifier;
import net.imglib2.labkit.utils.ParallelUtils;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class SegmenterPanel {

	private final SegmenterListModel segmentationModel;

	private final JPanel panel = new JPanel();

	private final ComponentList<SegmentationItem, JPanel> list = new ComponentList<>();

	private final Function<Supplier<SegmentationItem>, JPopupMenu> menuFactory;

	private JButton addSegmenterButton;

	public SegmenterPanel(
		SegmenterListModel segmentationModel,
		Function<Supplier<SegmentationItem>, JPopupMenu> menuFactory)
	{
		this.segmentationModel = segmentationModel;
		this.menuFactory = menuFactory;
		panel.setLayout(new MigLayout("insets 0, gap 0", "[grow]", "[grow][]"));
		panel.add(initList(), "grow, wrap");
		panel.add(initBottomPanel(), "grow");
	}

	public static JPanel newFramedSegmeterPanel(
		SegmenterListModel segmentationModel,
		DefaultExtensible extensible)
	{
		return GuiUtils.createCheckboxGroupedPanel(segmentationModel
			.segmentationVisibility(), "Segmentation", new SegmenterPanel(
				segmentationModel, item -> extensible.createPopupMenu(
					SegmentationItem.SEGMENTER_MENU, item)).getComponent());
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
		list.clear();
		segmentationModel.segmenters().get().forEach(item -> list.add(item,
			new EntryPanel(item)));
		list.setSelected(segmentationModel.selectedSegmenter().get());
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
			item.editSettings(null);
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
		list.listeners().add(this::userChangedSelection);
		segmentationModel.segmenters().notifier().add(this::updateList);
		JComponent component = list.getComponent();
		component.setBorder(BorderFactory.createEmptyBorder());
		return component;
	}

	private void userChangedSelection() {
		SegmentationItem selectedValue = list.getSelected();
		if (selectedValue != null)
			segmentationModel.selectedSegmenter().set(selectedValue);
	}

	public JComponent getComponent() {
		return panel;
	}
}
