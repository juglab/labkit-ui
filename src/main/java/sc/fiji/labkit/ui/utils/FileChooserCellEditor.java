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

package sc.fiji.labkit.ui.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.File;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

public class FileChooserCellEditor extends DefaultCellEditor implements TableCellEditor {

	/** Number of clicks to start editing */
	private static final int CLICK_COUNT_TO_START = 2;
	/** Editor component */
	private JButton button;
	/** File chooser */
	private JFileChooser fileChooser;
	/** Selected file */
	private String file = "";

	/**
	 * Constructor.
	 */
	public FileChooserCellEditor() {
		super(new JTextField());
		setClickCountToStart(CLICK_COUNT_TO_START);

		// Using a JButton as the editor component
		button = new JButton();
		button.setBackground(Color.white);
		button.setFont(button.getFont().deriveFont(Font.PLAIN));
		button.setBorder(null);

		// Dialog which will do the actual editing
		fileChooser = new JFileChooser();
	}

	@Override
	public Object getCellEditorValue() {
		return file;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
		int row, int column)
	{
		file = value.toString();
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				fileChooser.setSelectedFile(new File(file));
				if (fileChooser.showOpenDialog(button) == JFileChooser.APPROVE_OPTION) {
					file = fileChooser.getSelectedFile().getAbsolutePath();
				}
				fireEditingStopped();
			}
		});
		button.setText(file);
		return button;
	}
}
