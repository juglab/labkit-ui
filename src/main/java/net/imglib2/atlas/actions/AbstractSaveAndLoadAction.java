package net.imglib2.atlas.actions;

import net.imglib2.atlas.MainFrame;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author Matthias Arzt
 */
public abstract class AbstractSaveAndLoadAction {

	private final MainFrame.Extensible extensible;

	public AbstractSaveAndLoadAction(MainFrame.Extensible extensible) {
		this.extensible = extensible;
	}

	public void initAction(String title, Action action, String keyStroke) {
		extensible.addAction(new AbstractNamedAction(title) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				fileChooserAndThen(filename -> {
					try {
						action.run(filename);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}
		}, keyStroke);
	}

	private void fileChooserAndThen(Consumer<String> action) {
		final JFileChooser fileChooser = new JFileChooser();
		final int returnVal = fileChooser.showOpenDialog(extensible.dialogParent());
		if ( returnVal == JFileChooser.APPROVE_OPTION )
			action.accept(fileChooser.getSelectedFile().getAbsolutePath());
	}

	public interface Action {
		void run(String filename) throws Exception;
	}
}
