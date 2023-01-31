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

import net.miginfocom.swing.MigLayout;
import org.scijava.Context;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Dialog that allows to create a new Labkit project.
 */
public class NewProjectDialog extends JDialog {

	private final JTextField textField;

	private final LabkitProjectModel model;

	private boolean approved = false;

	private NewProjectDialog(Context context) {
		super((Frame) null);
		setTitle("New Project");
		setLayout(new MigLayout());
		add(new JLabel("Project folder:"));
		textField = new JTextField();
		add(textField, "growx, pushx");
		add(newButton("...", ignore -> onSelectFolderClicked()), "wrap");
		add(new JLabel("Add images to the project:"), "span, wrap");
		model = new LabkitProjectModel(context, null, new ArrayList<>());
		add(new LabkitProjectEditor(model), "grow, span, wrap");
		add(newButton("Create Project", ignore -> onCreateProjectClicked()));
		add(newButton("Cancel", ignore -> onCancelClicked()));
	}

	private void onCancelClicked() {
		dispose();
	}

	private void onCreateProjectClicked() {
		this.approved = true;
		dispose();
	}

	private JButton newButton(String text, ActionListener actionListener) {
		JButton selectFolderButton = new JButton(text);
		selectFolderButton.addActionListener(actionListener);
		return selectFolderButton;
	}

	public static LabkitProjectModel show(Context context) {
		NewProjectDialog dialog = new NewProjectDialog(context);
		dialog.pack();
		dialog.setModal(true);
		dialog.setResizable(true);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
		if (dialog.approved) {
			LabkitProjectModel newProject = dialog.getModel();
			try {
				LabkitProjectFileSerializer.save(newProject, new File(newProject.getProjectDirectory(),
					"labkit-project.yaml"));
			}
			catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Error while saving:\n" + e.getMessage(),
					"Create New Labkit Project", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			return newProject;
		}
		return null;
	}

	private LabkitProjectModel getModel() {
		String projectDirectory = textField.getText();
		return new LabkitProjectModel(model.context(), projectDirectory, model.labeledImages());
	}

	private void onSelectFolderClicked() {
		JFileChooser dialog = new JFileChooser();
		dialog.setCurrentDirectory(new File(textField.getText()));
		dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		dialog.setDialogTitle("Select Project Folder");
		int returnValue = dialog.showOpenDialog(this);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File file = dialog.getSelectedFile();
			if (checkFile(file))
				textField.setText(file.getAbsolutePath());
		}
	}

	private boolean checkFile(File file) {
		if (!file.isDirectory()) {
			JOptionPane.showMessageDialog(this, "Please select a directory.");
			return false;
		}
		if (containsLabkitProjectFiles(file)) {
			int option = JOptionPane.showConfirmDialog(this,
				"The selected directory seems to already contain a Labkit project. Do you want to override it?",
				"Warning", JOptionPane.YES_NO_OPTION);
			return option == JOptionPane.OK_OPTION;
		}
		return true;
	}

	private boolean containsLabkitProjectFiles(File file) {
		return new File(file, "labkit-project.yaml").exists();
	}

	public static void main(String... args) {
		show(null);
	}
}
