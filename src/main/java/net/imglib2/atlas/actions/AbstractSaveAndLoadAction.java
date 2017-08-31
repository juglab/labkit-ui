package net.imglib2.atlas.actions;

import net.imglib2.atlas.MainFrame;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Matthias Arzt
 */
public abstract class AbstractSaveAndLoadAction {

	public static final FileFilter TIFF_FILTER = new FileNameExtensionFilter("TIF Image (*.tif, *.tiff)", "tif", "tiff");

	private final MainFrame.Extensible extensible;

	private final JFileChooser fileChooser;

	public AbstractSaveAndLoadAction(MainFrame.Extensible extensible, FileFilter fileFilter) {
		this.extensible = extensible;
		this.fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(fileFilter);
		fileChooser.setAcceptAllFileFilterUsed(true);
		fileChooser.setFileFilter(fileFilter);
	}


	public void initSaveAction(String title, Action action, String keyStroke) {
		initAction(title, action, keyStroke, true);
	}

	public void initLoadAction(String title, Action action, String keyStroke) {
		initAction(title, action, keyStroke, false);
	}

	public void initAction(String title, Action action, String keyStroke, boolean save) {
		extensible.addAction(new AbstractNamedAction(title) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				OpenDialogAndThen(title, save, filename -> {
					try {
						action.run(filename);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}
		}, keyStroke);
	}

	private void OpenDialogAndThen(String title, boolean save, Consumer<String> action) {
		fileChooser.setDialogTitle(title);
		fileChooser.setDialogType(save ? JFileChooser.SAVE_DIALOG : JFileChooser.OPEN_DIALOG);
		final int returnVal = fileChooser.showDialog(extensible.dialogParent(), null);
		if ( returnVal == JFileChooser.APPROVE_OPTION )
			action.accept(fileChooser.getSelectedFile().getAbsolutePath());
	}

	public interface Action {
		void run(String filename) throws Exception;
	}
}
