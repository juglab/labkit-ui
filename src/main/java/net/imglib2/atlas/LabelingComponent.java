package net.imglib2.atlas;

import bdv.util.*;
import bdv.viewer.DisplayMode;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.atlas.actions.ToggleVisibility;
import net.imglib2.atlas.color.ColorMapColorProvider;
import net.imglib2.atlas.color.IntegerARGBConverters;
import net.imglib2.atlas.color.UpdateColormap;
import net.imglib2.atlas.control.brush.*;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LabelingComponent {

	private BdvHandle bdvHandle;

	private InputTriggerConfig config = new InputTriggerConfig();

	private Actions actions = new Actions( config );

	private Behaviours behaviors = new Behaviours(config);

	private JPanel panel = new JPanel();

	private final JFrame dialogBoxOwner;

	private final List<AbstractNamedAction> actionsList = new ArrayList();

	private ColorMapColorProvider colorProvider;

	private LabelBrushController brushController;

	public LabelingComponent(JFrame dialogBoxOwner) {
		this.dialogBoxOwner = dialogBoxOwner;
	}

	public JComponent getComponent() {
		return panel;
	}

	public List<AbstractNamedAction> getActions() {
		return actionsList;
	}

	@SuppressWarnings( { "rawtypes" } )
	public < R extends RealType< R > >
	BdvHandle trainClassifier(
			final RandomAccessibleInterval<R> rawData,
			final int nLabels,
			final CellGrid grid,
			final boolean isTimeSeries) throws IOException
	{
		final Interval interval = new FinalInterval(rawData);
		final int nDim = rawData.numDimensions();

		colorProvider = new ColorMapColorProvider(LabelBrushController.BACKGROUND, 0 );

		initBdv(isTimeSeries && nDim == 3);

		brushController = initLabelsLayer(nLabels, grid, isTimeSeries, colorProvider);

		addAction(new ToggleVisibility( "Toggle Classification", bdvHandle.getViewerPanel(), 1 ), "C");

		BdvFunctions.show(rawData, "original", BdvOptions.options().addTo( bdvHandle ));

		return bdvHandle;
	}

	private static int[] cellDimensions(CellGrid grid) {
		final int[] cellDimensions = new int[ grid.numDimensions() ];
		grid.cellDimensions( cellDimensions );
		return cellDimensions;
	}

	public void addAction(AbstractNamedAction action, String keyStroke) {
		JMenuItem item = new JMenuItem(action);
		actionsList.add(action);
		actions.namedAction(action, keyStroke);
		actions.install( bdvHandle.getKeybindings(), "classifier training" );
	}

	private void initBdv(boolean is2D) {
		final BdvOptions options = BdvOptions.options();
		if (is2D)
			options.is2D();
		bdvHandle = new BdvHandlePanel(dialogBoxOwner, options);
		panel.setLayout(new BorderLayout());
		panel.add(bdvHandle.getViewerPanel());
		bdvHandle.getViewerPanel().setDisplayMode( DisplayMode.FUSED );
	}

	private PaintPixelsGenerator<IntType, ? extends Iterator<IntType>> initPixelGenerator(boolean isTimeSeries, int numDimensions) {
		if ( isTimeSeries )
			return new NeighborhoodPixelsGeneratorForTimeSeries<>(numDimensions - 1, new NeighborhoodPixelsGenerator<IntType>(NeighborhoodFactories.hyperSphere(), 1.0));
		else
			return new NeighborhoodPixelsGenerator<>( NeighborhoodFactories.< IntType >hyperSphere(), 1.0 );
	}

	private Img<IntType> initCachedLabelsImg(CellGrid grid, int[] cellDimensions) {
		final DiskCachedCellImgOptions labelsOpt = DiskCachedCellImgOptions.options().cellDimensions( cellDimensions ).dirtyAccesses( true );
		final DiskCachedCellImgFactory< IntType > labelsFac = new DiskCachedCellImgFactory<>( labelsOpt );
		CellLoader<IntType> loader = target -> target.forEach(x -> x.set(LabelBrushController.BACKGROUND));
		return labelsFac.create( grid.getImgDimensions(), new IntType(), loader);
	}

	private LabelBrushController initLabelsLayer(int nLabels, CellGrid grid, boolean isTimeSeries, ColorMapColorProvider colorProvider) {
		final int[] cellDimensions = cellDimensions(grid);
		final Img<IntType> labels = initCachedLabelsImg(grid, cellDimensions);
		BdvFunctions.show( Converters.convert( (RandomAccessibleInterval< IntType >) labels, new IntegerARGBConverters.ARGB<>( colorProvider ), new ARGBType() ), "labels", BdvOptions.options().addTo(bdvHandle) );
		final LabelBrushController brushController = new LabelBrushController(
				bdvHandle.getViewerPanel(),
				labels,
				initPixelGenerator(isTimeSeries, labels.numDimensions()),
				behaviors,
				nLabels,
				LabelBrushController.emptyGroundTruth(),
				colorProvider );
		behaviors.install( bdvHandle.getTriggerbindings(), "classifier training" );
		initColorMapUpdaterAction(nLabels, colorProvider);
		addAction(new ToggleVisibility( "Toggle Labels", bdvHandle.getViewerPanel(), 0 ), "L");
		bdvHandle.getViewerPanel().getDisplay().addOverlayRenderer( brushController.getBrushOverlay() );
		return brushController;
	}

	private void initColorMapUpdaterAction(int nLabels, ColorMapColorProvider colorProvider) {
		final UpdateColormap colormapUpdater = new UpdateColormap( colorProvider, nLabels, bdvHandle.getViewerPanel(), 1.0f );
		colormapUpdater.updateColormap();
		addAction(colormapUpdater, "ctrl shift C");
	}

	public ColorMapColorProvider colorProvider() {
		return colorProvider;
	}

	public LabelBrushController brushController() {
		return brushController;
	}

	public Behaviours behaviors() {
		return behaviors;
	}
}
