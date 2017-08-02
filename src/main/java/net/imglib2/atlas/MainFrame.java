package net.imglib2.atlas;

import bdv.util.BdvHandle;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.atlas.classification.Classifier;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.RealType;

import java.io.IOException;
import java.util.List;

/**
 * A component that supports labeling an image.
 *
 * @author Matthias Arzt
 */
public class MainFrame {

	public < R extends RealType< R >, F extends RealType<F> >
	BdvHandle trainClassifier(
			final RandomAccessibleInterval<R> rawData,
			final List<? extends RandomAccessibleInterval<F>> features,
			final Classifier classifier,
			final int nLabels,
			final CellGrid grid,
			final boolean isTimeSeries) throws IOException
	{
		return new LabelingComponent<F>().trainClassifier(rawData, features, classifier, nLabels, grid, isTimeSeries);
	}
}
