
package net.imglib2.labkit.actions;

import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.menu.MenuKey;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Matthias Arzt
 */
public abstract class AbstractFileIoAction {

	public static final FileFilter TIFF_FILTER = new FileNameExtensionFilter(
		"TIF Image (*.tif, *.tiff)", "tif", "tiff");
	public static final FileFilter LABELING_FILTER = new FileNameExtensionFilter(
		"Labeling (*.labeling)", "labeling");
	public static final FileFilter HDF5_FILTER = new FileNameExtensionFilter(
		"HDF5 + XML (*.h5, *.xml)", "h5", "xml");

	private final Extensible extensible;

	private final JFileChooser fileChooser;

	public AbstractFileIoAction(Extensible extensible,
		FileFilter... fileFilters)
	{
		this.extensible = extensible;
		this.fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		for (FileFilter fileFilter : fileFilters)
			fileChooser.addChoosableFileFilter(fileFilter);
		fileChooser.setAcceptAllFileFilterUsed(true);
		fileChooser.setFileFilter(fileFilters[0]);
	}

	public <T> void initSaveAction(MenuKey<T> menuKey, String title,
		float priority, Action<T> action, String keyStroke)
	{
		initAction(menuKey, title, priority, action, keyStroke,
			JFileChooser.SAVE_DIALOG);
	}

	public <T> void initOpenAction(MenuKey<T> menuKey, String title,
		float priority, Action<T> action, String keyStroke)
	{
		initAction(menuKey, title, priority, action, keyStroke,
			JFileChooser.OPEN_DIALOG);
	}

	private <T> void initAction(MenuKey<T> menuKey, String title, float priority,
		Action<T> action, String keyStroke, int dialogType)
	{
		extensible.addMenuItem(menuKey, title, priority, data -> openDialogAndThen(
			title, dialogType, data, action), null, keyStroke);
	}

	protected <T> void openDialogAndThen(String title, int dialogType, T data,
		Action<T> action)
	{
		fileChooser.setDialogTitle(title);
		String filename = action.suggestedFile();
		if (filename != null) fileChooser.setSelectedFile(new File(filename));
		fileChooser.setDialogType(dialogType);
		final int returnVal = fileChooser.showDialog(extensible.dialogParent(),
			null);
		if (returnVal == JFileChooser.APPROVE_OPTION) runAction(data, action,
			getSelectedFile());
	}

	private String getSelectedFile() {
		final String path = fileChooser.getSelectedFile().getAbsolutePath();
		final boolean exists = new File(path).exists();
		final String extension = FilenameUtils.getExtension(path);
		if (fileChooser.getDialogType() == JFileChooser.SAVE_DIALOG && !exists &&
			(extension == null || extension.isEmpty()))
		{
			FileFilter filter = fileChooser.getFileFilter();
			if (filter instanceof FileNameExtensionFilter) {
				return path + "." + ((FileNameExtensionFilter) filter)
					.getExtensions()[0];
			}
		}
		return path;
	}

	private <T> void runAction(T data, Action<T> action, String filename) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(() -> {
			try {
				action.run(data, filename);
			}
			catch (CancellationException e) {
				// ignore it was just cancelled
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				executor.shutdown();
			}
		});
	}

	public interface Action<T> {

		default String suggestedFile() {
			return null;
		}

		void run(T data, String filename) throws Exception;
	}
}
