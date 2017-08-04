package net.imglib2.atlas;

import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import hr.irb.fastRandomForest.FastRandomForest;
import net.imglib2.*;
import net.imglib2.algorithm.features.FeatureGroup;
import net.imglib2.algorithm.features.Features;
import net.imglib2.algorithm.features.gui.FeatureSettingsGui;
import net.imglib2.atlas.actions.DeserializeClassifier;
import net.imglib2.atlas.actions.SerializeClassifier;
import net.imglib2.atlas.classification.Classifier;
import net.imglib2.atlas.classification.TrainClassifier;
import net.imglib2.atlas.classification.PredictionLayer;
import net.imglib2.atlas.classification.weka.TrainableSegmentationClassifier;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealFloatConverter;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.ui.OverlayRenderer;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.imglib2.algorithm.features.GroupedFeatures.*;
import static net.imglib2.algorithm.features.SingleFeatures.*;

/**
 * A component that supports labeling an image.
 *
 * @author Matthias Arzt
 */
public class MainFrame {

	private JFrame frame = initFrame();

	private Classifier classifier;

	private SharedQueue queue = new SharedQueue(Runtime.getRuntime().availableProcessors());

	private BdvHandle bdvHandle;

	private LabelingComponent labelingComponent;

	private FeatureStack featureStack;

	private List<String> classLabels = Arrays.asList("foreground", "background");

	private Extensible extensible = new Extensible();

	public < R extends RealType< R > >
	void trainClassifier(
			final RandomAccessibleInterval<R> rawData,
			final CellGrid grid,
			final boolean isTimeSeries) throws IOException
	{
		labelingComponent = new LabelingComponent(frame);

		int nLabels = classLabels.size();
		bdvHandle = labelingComponent.trainClassifier(rawData, nLabels, grid, isTimeSeries);
		// --
		FeatureGroup featureGroup = Features.group(SingleFeatures.identity(), GroupedFeatures.gauss());
		classifier = new TrainableSegmentationClassifier(FastRandomForest::new, classLabels, featureGroup);
		featureStack = new FeatureStack(toFloatType(rawData), classifier, grid);
		initClassification(rawData, nLabels);
		// --
		initMenu(labelingComponent.getActions());
		frame.add(labelingComponent.getComponent());
		frame.setVisible(true);
	}

	private <R extends RealType<R>> RandomAccessibleInterval<FloatType> toFloatType(RandomAccessibleInterval<R> in) {
		return Converters.convert(in, new RealFloatConverter<>(), new FloatType() );
	}

	private <R extends RealType<R>> void initClassification(RandomAccessibleInterval<R> rawData, int nLabels) {
		new TrainClassifier<>(extensible, this.classifier, labelingComponent.labelingMap(), featureStack.block());
		new PredictionLayer(extensible, labelingComponent.colorProvider(), classifier, featureStack);
		new SerializeClassifier(extensible, this.classifier);
		new DeserializeClassifier(extensible, this.classifier);
		new FeatureLayer(extensible, featureStack);
	}

	private JFrame initFrame() {
		JFrame frame = new JFrame("ATLAS");
		frame.setBounds( 50, 50, 1200, 900 );
		return frame;
	}

	private void initMenu(List<AbstractNamedAction> actions) {
		MenuBar bar = new MenuBar();
		JMenu others = new JMenu("others");
		others.add(newMenuItem("Change Feature Settings", this::changeFeatureSettings));
		bar.add(others);
		actions.forEach(bar::add);
		frame.setJMenuBar(bar);
	}

	private void changeFeatureSettings() {
		Optional<FeatureGroup> fg = FeatureSettingsGui.show();
		if(!fg.isPresent())
			return;
		classifier.reset(fg.get(), classLabels);
	}

	private JMenuItem newMenuItem(String title, Runnable runnable) {
		JMenuItem item = new JMenuItem(title);
		item.addActionListener(a -> runnable.run());
		return item;
	}

	public class Extensible {

		private Extensible() {

		}

		public void repaint() {
			bdvHandle.getViewerPanel().requestRepaint();
		}

		public void addAction(AbstractNamedAction action, String keyStroke) {
			labelingComponent.addAction(action, keyStroke);
		}

		public < T, V extends Volatile< T >> RandomAccessibleInterval< V > wrapAsVolatile(
				RandomAccessibleInterval<T> img)
		{
			return VolatileViews.wrapAsVolatile( img, queue );
		}

		public Object viewerSync() {
			return bdvHandle.getViewerPanel();
		}

		public <T extends NumericType<T>> BdvStackSource<T> addLayer(RandomAccessibleInterval<T> interval, String prediction) {
			return BdvFunctions.show(interval, prediction, BdvOptions.options().addTo(bdvHandle));
		}

		public Component dialogParent() {
			return frame;
		}

		public void addBehaviour(Behaviour behaviour, String name, String defaultTriggers) {
			labelingComponent.addBehaviour(behaviour, name, defaultTriggers);
		}

		public void addOverlayRenderer(OverlayRenderer overlay) {
			bdvHandle.getViewerPanel().getDisplay().addOverlayRenderer(overlay);
		}

		public void displayRepaint() {
			bdvHandle.getViewerPanel().getDisplay().repaint();
		}
	}
}
