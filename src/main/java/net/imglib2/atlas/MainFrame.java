package net.imglib2.atlas;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.atlas.classification.Classifier;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.RealType;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

/**
 * A component that supports labeling an image.
 *
 * @author Matthias Arzt
 */
public class MainFrame {

	private JFrame frame = initFrame();

	public < R extends RealType< R >, F extends RealType<F> >
	void trainClassifier(
			final RandomAccessibleInterval<R> rawData,
			final List<? extends RandomAccessibleInterval<F>> features,
			final Classifier classifier,
			final int nLabels,
			final CellGrid grid,
			final boolean isTimeSeries) throws IOException
	{
		LabelingComponent<F> labelingComponent = new LabelingComponent<F>(frame);
		labelingComponent.trainClassifier(rawData, features, classifier, nLabels, grid, isTimeSeries);
		initMenu(labelingComponent.getActions());
		frame.add(labelingComponent.getComponent());
		frame.setVisible(true);
	}

	private JFrame initFrame() {
		JFrame frame = new JFrame("ATLAS");
		frame.setBounds( 50, 50, 1200, 900 );
		return frame;
	}

	private void initMenu(List<AbstractNamedAction> actions) {
		MenuBar bar = new MenuBar();
		actions.forEach(bar::add);
		frame.setJMenuBar(bar);
	}

}
