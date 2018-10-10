
package net.imglib2.labkit.actions;

import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.menu.MenuKey;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

/**
 * @author Matthias Arzt
 */
public abstract class AbstractFileIoAction {

	public static final FileFilter TIFF_FILTER = new FileNameExtensionFilter(
		"TIF Image (*.tif, *.tiff)", "tif", "tiff");
	public static final FileFilter LABELING_FILTER = new FileNameExtensionFilter(
		"Labeling (*.labeling)", "labeling" );

	private final Extensible extensible;

	private final JFileChooser fileChooser;

	public AbstractFileIoAction(Extensible extensible, FileFilter... fileFilters) {
		this.extensible = extensible;
		this.fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		for(FileFilter fileFilter : fileFilters)
			fileChooser.addChoosableFileFilter(fileFilter);
		fileChooser.setAcceptAllFileFilterUsed(true);
		fileChooser.setFileFilter(fileFilters[0]);
	}

	public void initSaveAction(MenuKey<Void> menuKey, String title,
		float priority, Action action, String keyStroke)
	{
		initAction(menuKey, title, priority, action, keyStroke,
			JFileChooser.SAVE_DIALOG);
	}

	public void initOpenAction(MenuKey<Void> menuKey, String title,
		float priority, Action action, String keyStroke)
	{
		initAction(menuKey, title, priority, action, keyStroke,
			JFileChooser.OPEN_DIALOG);
	}

	private void initAction(MenuKey<Void> menuKey, String title, float priority,
		Action action, String keyStroke, int dialogType)
	{
		extensible.addMenuItem(menuKey, title, priority,
			ignore -> openDialogAndThen(title, dialogType, action), null, keyStroke);
	}

	protected void openDialogAndThen(String title, int dialogType,
		Action action)
	{
		fileChooser.setDialogTitle(title);
		String filename = action.suggestedFile();
		if (filename != null) fileChooser.setSelectedFile(new File(filename));
		fileChooser.setDialogType(dialogType);
		final int returnVal = fileChooser.showDialog(extensible.dialogParent(),
			null);
		if (returnVal == JFileChooser.APPROVE_OPTION) runAction(action,
				getSelectedFile());
	}

	private String getSelectedFile() {
		final String path =
				fileChooser.getSelectedFile().getAbsolutePath();
		final boolean exists = new File(path).exists();
		final String extension = FilenameUtils.getExtension(path);
		if(fileChooser.getDialogType() == JFileChooser.SAVE_DIALOG && !exists && (extension == null || extension.isEmpty())) {
			FileFilter filter = fileChooser.getFileFilter();
			if(filter instanceof FileNameExtensionFilter ) {
				return path + "." +
						((FileNameExtensionFilter) filter).getExtensions()[0];
			}
		}
		return path;
	}

	private void runAction(Action action, String filename) {
		try {
			action.run(filename);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public interface Action {

		default String suggestedFile() {
			return null;
		}

		void run(String filename) throws Exception;
	}
}
