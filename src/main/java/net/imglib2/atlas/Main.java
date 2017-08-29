package net.imglib2.atlas;

import javax.swing.*;
import java.util.function.Consumer;

/**
 * Created by arzt on 29.08.17.
 */
public class Main {
	public static void main(String... args) {
		fileChooserAndThen(filename -> PaintLabelsAndTrain.start(filename));
	}

	static private void fileChooserAndThen(Consumer<String> action) {
		final JFileChooser fileChooser = new JFileChooser();
		final int returnVal = fileChooser.showOpenDialog(null);
		if ( returnVal == JFileChooser.APPROVE_OPTION )
			action.accept(fileChooser.getSelectedFile().getAbsolutePath());
	}
}
