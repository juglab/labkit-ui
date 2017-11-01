package net.imglib2.atlas.actions;

import net.imglib2.atlas.MainFrame;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
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


	public void initSaveAction(String title, String command, Action action, String keyStroke) {
		initAction(title, command, action, keyStroke, true);
	}

	public void initLoadAction(String title, String command, Action action, String keyStroke) {
		initAction(title, command, action, keyStroke, false);
	}

	public void initAction(String title, String command, Action action, String keyStroke, boolean save) {
		extensible.addAction(title, command, () -> OpenDialogAndThen(title, save, action), keyStroke);
	}

	private void OpenDialogAndThen(String title, boolean save, Consumer<String> action) {
		fileChooser.setDialogTitle(title);
		fileChooser.setDialogType(save ? JFileChooser.SAVE_DIALOG : JFileChooser.OPEN_DIALOG);
		final int returnVal = fileChooser.showDialog(extensible.dialogParent(), null);
		if ( returnVal == JFileChooser.APPROVE_OPTION )
			action.accept(fileChooser.getSelectedFile().getAbsolutePath());
	}

	public interface Action extends Consumer<String> {
		@Override
		default void accept(String filename) {
			try {
				run(filename);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		void run(String filename) throws Exception;
	}
}
