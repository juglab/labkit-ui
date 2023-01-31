/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.ui.project;

import sc.fiji.labkit.ui.actions.AbstractFileIoAction;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A Panel the shows a list of labeled images.
 */
public class LabeledImagesListPanel extends JPanel {

	private final LabkitProjectModel model;

	private final JList<LabeledImage> list;

	private final MyListModel listModel = new MyListModel();

	public LabeledImagesListPanel(LabkitProjectModel model) {
		this.model = model;
		setLayout(new MigLayout("", "[grow]", "[grow]0px[]"));
		this.list = initList(model);
		list.setModel(listModel);
		model.changeNotifier().addListener(listModel::triggerUpdate);
		add(initScrollPane(list), "grow, wrap");
		JPanel buttonsPanel = initButtonsPanel();
		buttonsPanel.setBackground(list.getBackground());
		add(buttonsPanel, "grow");
	}

	private JScrollPane initScrollPane(JList<LabeledImage> list) {
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		return scrollPane;
	}

	private JPanel initButtonsPanel() {
		JPanel buttonsPanel = new JPanel(new MigLayout());
		JButton addImageButton = new JButton("Add image");
		addImageButton.addActionListener(ignore -> onAddImageClicked(addImageButton));
		buttonsPanel.add(addImageButton);
		JButton editProjectButton = new JButton("Edit project");
		editProjectButton.addActionListener(ignore -> onEditProjectButtonClicked());
		buttonsPanel.add(editProjectButton);
		return buttonsPanel;
	}

	private void onAddImageClicked(JButton button) {
		JFileChooser chooser = new JFileChooser("Select Image");
		chooser.setFileFilter(AbstractFileIoAction.TIFF_FILTER);
		chooser.setMultiSelectionEnabled(true);
		int result = chooser.showOpenDialog(button);
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

	private void onEditProjectButtonClicked() {
		LabkitProjectEditor.show(model);
	}

	private static JList<LabeledImage> initList(LabkitProjectModel labkitProjectModel) {
		JList<LabeledImage> list = new JList<>(labkitProjectModel.labeledImages().toArray(
			new LabeledImage[0]));
		list.setCellRenderer(new MyListItemRenderer());
		list.addListSelectionListener(event -> {
			if (!event.getValueIsAdjusting()) {
				labkitProjectModel.selectedImage().set(list.getSelectedValue());
			}
		});
		return list;
	}

	private class MyListModel implements ListModel<LabeledImage> {

		private final List<ListDataListener> listeners = new CopyOnWriteArrayList<>();

		private final Set<LabeledImage> observedElements = new HashSet<>();

		private final Runnable updateListener = this::triggerUpdate;

		private void triggerUpdate() {
			unregisterAll();
			ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, model
				.labeledImages().size());
			listeners.forEach(l -> l.contentsChanged(e));
		}

		private void unregisterAll() {
			synchronized (observedElements) {
				for (LabeledImage labeledImage : observedElements)
					labeledImage.modified().notifier().removeListener(updateListener);
			}
			observedElements.clear();
		}

		private void register(LabeledImage labeledImage) {
			// The list needs to be updated if the labeled images state changes between
			// unmodified and modified.
			synchronized (observedElements) {
				if (observedElements.add(labeledImage))
					labeledImage.modified().notifier().addListener(updateListener);
			}
		}

		@Override
		public int getSize() {
			return model.labeledImages().size();
		}

		@Override
		public LabeledImage getElementAt(int index) {
			LabeledImage labeledImage = model.labeledImages().get(index);
			register(labeledImage);
			return labeledImage;
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

	private static class MyListItemRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,
			boolean isSelected, boolean cellHasFocus)
		{
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof LabeledImage && ((LabeledImage) value).modified().get())
				setForeground(Color.blue);
			return this;
		}
	}
}
