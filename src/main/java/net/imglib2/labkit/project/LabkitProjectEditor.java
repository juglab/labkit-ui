
package net.imglib2.labkit.project;

import net.imglib2.labkit.actions.AbstractFileIoAction;
import net.imglib2.labkit.utils.FileChooserCellEditor;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.UnaryOperator;

/**
 * Implements a dialog that shows the content of a Labkit project as table.
 */
public class LabkitProjectEditor extends JPanel {

	private final LabkitProjectModel model;

	private final MyTableModel tableModel = new MyTableModel();

	private final JTable table;

	public LabkitProjectEditor(LabkitProjectModel model) {
		this.model = model;
		setLayout(new MigLayout("", "[grow]", "[][grow][]"));
		add(new JLabel("Double click on the table to make changes."), "wrap");
		table = new JTable(tableModel);
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(1).setCellEditor(new FileChooserCellEditor());
		columnModel.getColumn(2).setCellEditor(new FileChooserCellEditor());
		columnModel.getColumn(0).setPreferredWidth(50);
		columnModel.getColumn(1).setPreferredWidth(100);
		columnModel.getColumn(2).setPreferredWidth(100);
		add(new JScrollPane(table), "grow, wrap");
		add(newButton("Add Images", this::onAddImagesClicked), "split");
		add(newButton("Remove Selected", this::onRemoveSelectedClicked));
		model.changeNotifier().addListener(tableModel::updateView);
	}

	private void onAddImagesClicked() {
		JFileChooser chooser = new JFileChooser("Select Image");
		chooser.setFileFilter(AbstractFileIoAction.TIFF_FILTER);
		chooser.setMultiSelectionEnabled(true);
		int result = chooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File[] files = chooser.getSelectedFiles();
			for (File file : files) {
				String filename = file.getPath();
				LabeledImage newLabeledImage = new LabeledImage(model.context(), filename);
				model.labeledImages().add(newLabeledImage);
				model.changeNotifier().notifyListeners();
			}
		}
	}

	private void onRemoveSelectedClicked() {
		removeItems(model.labeledImages(), table.getSelectedRows());
		model.changeNotifier().notifyListeners();
	}

	private void removeItems(List<LabeledImage> list, int[] indices) {
		Arrays.sort(indices);
		for (int i = indices.length - 1; i >= 0; i--)
			list.remove(indices[i]);
	}

	private Component newButton(String label, Runnable action) {
		JButton button = new JButton(label);
		button.addActionListener(ignore -> action.run());
		return button;
	}

	private class MyTableModel implements TableModel {

		private final List<String> COLUMN_TITLES = Arrays.asList("Nick Name", "Image File",
			"Labeling File");

		private final List<TableModelListener> tableListeners = new CopyOnWriteArrayList<>();

		@Override
		public int getRowCount() {
			return model.labeledImages().size();
		}

		@Override
		public int getColumnCount() {
			return COLUMN_TITLES.size();
		}

		@Override
		public String getColumnName(int columnIndex) {
			return COLUMN_TITLES.get(columnIndex);
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			LabeledImage labeledImage = model.labeledImages().get(rowIndex);
			switch (columnIndex) {
				case 0:
					return labeledImage.getName();
				case 1:
					return labeledImage.getImageFile();
				case 2:
					return labeledImage.getLabelingFile();
			}
			throw new AssertionError();
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			switch (columnIndex) {
				case 0:
					setName(rowIndex, (String) value);
					return;
				case 1:
					setImage(rowIndex, (String) value);
					return;
				case 2:
					setLabeling(rowIndex, (String) value);
					return;
			}
			throw new AssertionError();
		}

		private void setName(int rowIndex, String newName) {
			modifyLabeledImage(rowIndex, oldLabeledImage -> {
				oldLabeledImage.setName(newName);
				return oldLabeledImage;
			});
		}

		private void setImage(int rowIndex, String newImageFile) {
			modifyLabeledImage(rowIndex, oldLabeledImage -> {
				String name = oldLabeledImage.getName();
				if (name.equals(FilenameUtils.getName(oldLabeledImage.getImageFile())))
					name = FilenameUtils.getName(newImageFile);
				return new LabeledImage(model.context(), name, newImageFile, oldLabeledImage
					.getLabelingFile());
			});
		}

		private void setLabeling(int rowIndex, String newLabelingFile) {
			modifyLabeledImage(rowIndex, oldLabeledImage -> new LabeledImage(model.context(),
				oldLabeledImage.getName(),
				oldLabeledImage.getImageFile(), newLabelingFile));
		}

		private void modifyLabeledImage(int rowIndex, UnaryOperator<LabeledImage> operator) {
			LabeledImage oldLabeledImage = model.labeledImages().get(rowIndex);
			LabeledImage newLabeledImage = operator.apply(oldLabeledImage);
			model.labeledImages().set(rowIndex, newLabeledImage);
			model.changeNotifier().notifyListeners();
		}

		@Override
		public void addTableModelListener(TableModelListener l) {
			tableListeners.add(l);
		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
			tableListeners.remove(l);
		}

		private void updateView() {
			TableModelEvent e = new TableModelEvent(this);
			tableListeners.forEach(l -> l.tableChanged(e));
		}
	}

	public static void show(LabkitProjectModel model) {
		JFrame frame = new JFrame("Edit Labkit Project");
		frame.add(new LabkitProjectEditor(model));
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
}
