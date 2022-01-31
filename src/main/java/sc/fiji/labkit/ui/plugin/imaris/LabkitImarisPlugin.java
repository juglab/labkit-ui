package sc.fiji.labkit.ui.plugin.imaris;

import Imaris.Error;
import bdv.export.ProgressWriter;
import bdv.export.ProgressWriterNull;
import com.bitplane.xt.ImarisApplication;
import com.bitplane.xt.ImarisDataset;
import com.bitplane.xt.ImarisService;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import sc.fiji.labkit.ui.InitialLabeling;
import sc.fiji.labkit.ui.models.DefaultSegmentationModel;
import sc.fiji.labkit.ui.models.ImageLabelingModel;
import sc.fiji.labkit.ui.models.SegmentationItem;
import sc.fiji.labkit.ui.models.SegmenterListModel;
import sc.fiji.labkit.ui.segmentation.ForwardingSegmenter;
import sc.fiji.labkit.ui.segmentation.Segmenter;
import sc.fiji.labkit.ui.segmentation.weka.PixelClassificationPlugin;
import sc.fiji.labkit.ui.segmentation.weka.TrainableSegmentationSegmenter;
import sc.fiji.labkit.ui.utils.ParallelUtils;

/**
 * @author Tobias Pietzsch
 */
@Plugin( type = Command.class, menuPath = "Plugins > Labkit > Open Current Imaris Image with Labkit" )
public class LabkitImarisPlugin implements Command
{
	@Parameter private Context context;

	@Parameter private ImarisService imaris;

	@Override
	public void run()
	{
		run( context, imaris.getApplication() );
	}

	private void run( final Context context, final ImarisApplication imaris )
	{
		final ImarisDataset< ? > dataset = imaris.getDataset();
		final ImarisInputImage< ? > input = new ImarisInputImage<>( dataset );

		final DefaultSegmentationModel model = new DefaultSegmentationModel( context, input );
		model.imageLabelingModel().labeling().set( InitialLabeling.initialLabeling( context, input ) );

		final ImarisExtensionPoints ext = new ImarisExtensionPoints( model, imaris, null );
		ext.registerImageFactories();

		final String title = input.imageForSegmentation().getName();
		show( model, ext, title, null, false, false, notifyOnWindowClosing( dataset ), imaris );
	}

	/**
	 * @param context
	 * @param imaris
	 * @param classifier
	 * @param headless
	 * 		whether to run headless
	 * 		If {@code headless == true}, load the given {@code classifier}, run
	 * 		segmentation on the current image from {@code imaris}, and send back the
	 * 		results.
	 * 		If {@code headless == false}, load the given classifier, and open the
	 * 		the current image from {@code imaris} in the Labkit window.
	 * @param storeClassifiersPath
	 * 		if {@code != null}, store each trained classifiers to the given path and
	 * 		notify Imaris by calling {@code dataset.SetParameter( "Labkit",
	 * 		"ClassifierFile", classifierFilename );}.
	 * @param closeLabkitAfterCalculatingResult
	 * 		if (@code true}, the Labkit window is closed after "Compute result
	 * 		and send it to Imaris" is done.
	 * @param useGpu
	 * 		if (@code true}, segmentation will use GPU.
	 */
	private void run(
			final Context context,
			final ImarisApplication imaris,
			final String classifier,
			final boolean headless,
			final String storeClassifiersPath,
			final boolean closeLabkitAfterCalculatingResult,
			final boolean useGpu)
	{
		final ImarisDataset< ? > dataset = imaris.getDataset();
		final ImarisInputImage< ? > input = new ImarisInputImage<>( dataset );

		final DefaultSegmentationModel model = new DefaultSegmentationModel( context, input );
		model.imageLabelingModel().labeling().set( InitialLabeling.initialLabeling( context, input ) );

		final ImarisExtensionPoints ext = new ImarisExtensionPoints( model, imaris, storeClassifiersPath );
		ext.registerImageFactories();

		if (headless) {
			segmentHeadless( model, ext, classifier, useGpu );
		} else {
			final String title = input.imageForSegmentation().getName();
			show( model, ext, title, classifier, useGpu, closeLabkitAfterCalculatingResult, notifyOnWindowClosing( dataset ), imaris );
		}
	}

	private static final Map< Integer, WeakReference< JFrame > > applicationIdToLabkitFrame = new ConcurrentHashMap<>();

	private static Runnable notifyOnWindowClosing( final ImarisDataset< ? > dataset )
	{
		return () -> {
			try
			{
				dataset.getIDataSetPrx().SetParameter( "Labkit", "LabkitWindowClosed", "true" );
			}
			catch ( Error error )
			{
				throw new RuntimeException( error );
			}
		};
	}

	private static void segmentHeadless( final DefaultSegmentationModel segmentationModel, final ImarisExtensionPoints ext, final String classifier, final boolean useGpu )
	{
		final SegmenterListModel segmenterList = segmentationModel.segmenterList();
		final SegmentationItem item = segmenterList.addSegmenter( PixelClassificationPlugin.create( useGpu ) );
		item.openModel( classifier );

		final ImageLabelingModel imageLabeling = segmentationModel.imageLabelingModel();
		final RandomAccessibleInterval< FloatType > prediction = item.results( imageLabeling ).prediction();
		ParallelUtils.runInOtherThread( () -> {
			final ProgressWriter progress = new ProgressWriterNull()
			{
				@Override
				public void setProgress( final double completionRatio )
				{
					ext.reportProgress( completionRatio );
				}
			};
			ParallelUtils.populateCachedImg( prediction, progress );
			ext.setImarisImage( prediction );
		} );
	}

	private static void show(
			final DefaultSegmentationModel model,
			final ImarisExtensionPoints ext,
			final String title,
			final String classifier,
			final boolean useGpu,
			final boolean closeLabkitAfterCalculatingResult,
			final Runnable onClose,
			final ImarisApplication imaris )
	{
		JFrame frame = new JFrame( frameTitle( title ) );
		frame.setBounds( 50, 50, 1200, 900 );
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		ImarisSegmentationComponent component = new ImarisSegmentationComponent( frame, model, ext, closeLabkitAfterCalculatingResult );
		if ( classifier != null )
			component.loadClassifier( classifier, useGpu );

		frame.setJMenuBar( component.getMenuBar() );
		frame.add( component );

		frame.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				if ( onClose != null )
					onClose.run();
			}
		} );

		applicationIdToLabkitFrame.put( imaris.getApplicationID(), new WeakReference<>( frame ) );
		frame.setVisible( true );
	}

	private static String frameTitle( final String title )
	{
		if ( title == null || title.isEmpty() )
			return "Labkit";
		else
			return "Labkit - " + title;

	}

	/**
	 * Entry point for starting Labkit directly from Imaris.
	 */
	public static void imageFromImaris( final String args )
	{
		final Context context = new Context();
		final ImarisService imaris = context.getService( ImarisService.class );

		List< cCommand > commands = CommandsFromString( args );
		int applicationId = -1;
		String endPoints = "default -p 4029";
		String classifier = null;
		boolean headless = false;
		String storeClassifiersPath = null;
		boolean closeLabkitAfterCalculatingResult = true;
		boolean useGpu = false;
		for ( cCommand aCommand : commands )
		{
			if ( aCommand.mName == "EndPoints" )
			{
				endPoints = aCommand.mParams;
			}
			else if ( aCommand.mName == "ApplicationID" )
			{
				applicationId = Integer.parseInt( aCommand.mParams );
			}
			else if ( aCommand.mName == "Classifier" )
			{
				classifier = aCommand.mParams;
			}
			else if ( aCommand.mName == "Headless" )
			{
				headless = Boolean.parseBoolean(aCommand.mParams.trim());
			}
			else if ( aCommand.mName == "StoreClassifiersPath" )
			{
				storeClassifiersPath = aCommand.mParams;
			}
			else if ( aCommand.mName == "CloseLabkitAfterCalculatingResult" )
			{
				closeLabkitAfterCalculatingResult = Boolean.parseBoolean(aCommand.mParams.trim());
			}
			else if ( aCommand.mName == "UseGPU" )
			{
				useGpu = Boolean.parseBoolean(aCommand.mParams.trim());
			}
		}

		final ImarisApplication app = ( applicationId == -1 )
				? imaris.getApplication()
				: imaris.getApplicationByID( applicationId );
		new LabkitImarisPlugin().run( context, app, classifier, headless, storeClassifiersPath, closeLabkitAfterCalculatingResult, useGpu );
	}

	/**
	 * Entry point for closing Labkit from Imaris.
	 */
	public static void closeLabkitWindow( final String args )
	{
		final Context context = new Context();
		final ImarisService imaris = context.getService( ImarisService.class );

		List< cCommand > commands = CommandsFromString( args );
		int applicationId = -1;
		for ( cCommand aCommand : commands )
		{
			if ( aCommand.mName == "ApplicationID" )
			{
				applicationId = Integer.parseInt( aCommand.mParams );
			}
		}
		final WeakReference< JFrame > frameRef = applicationIdToLabkitFrame.get( applicationId );
		if ( frameRef != null )
		{
			final JFrame frame = frameRef.get();
			if ( frame != null )
				SwingUtilities.invokeLater( () -> {
					frame.dispatchEvent( new WindowEvent( frame, WindowEvent.WINDOW_CLOSING ) );
				} );
		}
	}

	// -- Code below is copied from Imaris_Bridge --

	private static final String[] mCommandsString = { "EndPoints", "ApplicationID", "Classifier", "Headless", "StoreClassifiersPath", "UseGPU" };

	private static class cCommand {
		public cCommand(String aName) {
			mName = aName;
		}
		public String mName = "";
		public String mParams = "";
	}

	private static List< cCommand > CommandsFromString( String aOptions )
	{
		List< cCommand > vResult = new LinkedList<>();
		int vIndex = -1;
		for ( String vCommand : mCommandsString )
		{
			if ( aOptions.toUpperCase().startsWith( vCommand.toUpperCase() ) )
			{
				vIndex = 0;
				aOptions = "-" + aOptions;
				break;
			}
		}
		while ( vIndex >= 0 )
		{
			vIndex++;
			int vEnd0 = aOptions.indexOf( "-", vIndex ); // other command
			int vEnd1 = aOptions.indexOf( "\"", vIndex ); // params
			int vEnd = vEnd0;
			if ( vEnd0 < 0 && vEnd1 < 0 )
			{
				vEnd = aOptions.length();
			}
			else if ( vEnd0 < 0 || ( vEnd1 >= 0 && vEnd1 < vEnd0 ) )
			{
				vEnd = vEnd1;
			}
			String vTag = aOptions.substring( vIndex, vEnd );
			cCommand vNew = null;
			for ( String vCommand : mCommandsString )
			{
				if ( vTag.toUpperCase().startsWith( vCommand.toUpperCase() ) )
				{
					vNew = new cCommand( vCommand );
					vResult.add( vNew );
					if ( vTag.length() > vCommand.length() )
					{
						String vParams = vTag.substring( vCommand.length() );
						vNew.mParams = vParams;
					}
					break;
				}
			}
			vIndex = vEnd;
			if ( vEnd == vEnd1 )
			{
				vEnd = aOptions.indexOf( "\"", vIndex + 1 );
				if ( vEnd >= 0 )
				{
					if ( vNew != null )
					{
						String vParams = aOptions.substring( vIndex + 1, vEnd );
						vNew.mParams = vParams;
					}
					vIndex = vEnd + 1;
				}
			}
			vIndex = aOptions.indexOf( "-", vIndex );
		}
		return vResult;
	}
}
