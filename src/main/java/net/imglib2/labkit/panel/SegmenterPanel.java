
package net.imglib2.labkit.panel;

import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmenterListModel;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.awt.*;

public class SegmenterPanel {

	private final SegmenterListModel<? extends SegmentationItem> segmentationModel;

	private final JPanel panel = new JPanel();

	private final ComponentList<Object, JPanel> list = new ComponentList<>();

	public SegmenterPanel(
		SegmenterListModel<? extends SegmentationItem> segmentationModel)
	{
		this.segmentationModel = segmentationModel;
		panel.setLayout(new MigLayout("insets 0, gap 0", "[grow]", "[grow][]"));
		panel.add(initList(), "grow, wrap");
		panel.add(initBottomPanel(), "grow");
	}

	private JComponent initBottomPanel() {
		JPanel result = new JPanel();
		result.setBackground(UIManager.getColor("List.background"));
		result.setLayout(new MigLayout("insets 4pt, gap 4pt"));
		result.add(initAddButton());
		return result;
	}

	private JButton initAddButton() {
		return GuiUtils.createActionIconButton("Add classifier", new RunnableAction(
			"Add classifier", () -> {
				segmentationModel.addSegmenter();
				updateList();
			}), "/images/add.png");
	}

	private void updateList() {
		list.clear();
		segmentationModel.segmenters().forEach(item -> list.add(item,
			new EntryPanel(item)));
		list.setSelected(segmentationModel.selectedSegmenter().get());
	}

	private class EntryPanel extends JPanel {

		private final SegmentationItem item;

		private EntryPanel(SegmentationItem item) {
			this.item = item;
			setLayout(new MigLayout("inset 4, gap 4, fillx"));
			add(new JLabel(item.toString()), "push");
			add(initPopupMenuButton());
			add(initTrainButton());
		}

		private JButton initTrainButton() {
			JButton button = new JButton("train");
			button.setBorder(BorderFactory.createLineBorder(Color.gray));
			button.setContentAreaFilled(false);
			button.setOpaque(false);
			button.addActionListener(a -> {
				((SegmenterListModel) segmentationModel).train(item);
			});
			return button;
		}

		private JButton initPopupMenuButton() {
			JPopupMenu menu = initPopupMenu();
			JButton button = new JButton("...");
			button.setBorder(BorderFactory.createEmptyBorder());
			button.setContentAreaFilled(false);
			button.setOpaque(false);
			button.addActionListener(actionEvent -> {
				menu.show(button, 0, button.getHeight());
			});
			return button;
		}

		private JPopupMenu initPopupMenu() {
			JPopupMenu menu = new JPopupMenu();
			menu.add(settingsMenuItem());
			menu.add(removeMenuItem());
			return menu;
		}

		private JMenuItem settingsMenuItem() {
			JMenuItem result = new JMenuItem("Settings ...");
			result.addActionListener(a -> item.segmenter().editSettings(null));
			return result;
		}

		private JMenuItem removeMenuItem() {
			JMenuItem result = new JMenuItem("remove");
			result.addActionListener(a -> {
				((SegmenterListModel) segmentationModel).remove(item);
				updateList();
			});
			return result;
		}

	}

	private JComponent initList() {
		updateList();
		list.listeners().add(this::userChangedSelection);
		JComponent component = list.getComponent();
		component.setBorder(BorderFactory.createEmptyBorder());
		return component;
	}

	private void userChangedSelection() {
		Object selectedValue = list.getSelected();
		if (selectedValue != null) ((SegmenterListModel) segmentationModel)
			.selectedSegmenter().set(selectedValue);
	}

	public JComponent getComponent() {
		return panel;
	}
}
