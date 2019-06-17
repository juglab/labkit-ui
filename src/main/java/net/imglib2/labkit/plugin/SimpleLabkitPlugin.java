package net.imglib2.labkit.plugin;

import ij.ImagePlus;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.BatchSegmenter;
import net.imglib2.labkit.inputimage.DefaultInputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.labeling.LabelingSerializer;
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.utils.progress.StatusServiceProgressWriter;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.util.Intervals;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import static net.imglib2.labkit.labeling.LabelingSerializer.fromImageAndLabelSets;

@Plugin( type = Command.class, menuPath = "Plugins>Segmentation>Label (simple mode)" )
public class SimpleLabkitPlugin implements Command {

	@Parameter
	Img input;

	@Parameter
	Img<? extends IntegerType<?>> labeling;

	@Parameter(type = ItemIO.OUTPUT)
	Img output;

	@Parameter
	Context context;

	@Parameter
	StatusService statusService;

	@Override
	public void run() {

		// init segmentation model, serializer, labeling model
		DefaultSegmentationModel segmentationModel = new DefaultSegmentationModel( new DefaultInputImage(
				input ), context );
		final ImageLabelingModel labelingModel = segmentationModel
				.imageLabelingModel();

		// load labeling from labeling img
		LabelingSerializer.LabelsMetaData meta = new LabelingSerializer.LabelsMetaData(labeling);
		ImgLabeling<String, ?> imgLabeling = fromImageAndLabelSets(labeling, meta.asLabelSets());
		labelingModel.labeling().set( Labeling.fromImgLabeling(imgLabeling) );
		if ( labelingModel.labeling().get().getLabels().size() == 0 )
		{
			System.out.println( "no labels" );
			return;
		}

		// train
		segmentationModel.trainAndWait( segmentationModel
				.selectedSegmenter().get() );

		// run segmentation
		final ImagePlus segImgImagePlus = ImageJFunctions.wrap( input, "seginput" );
		final Img<ARGBType> segImg = ImageJFunctions.wrap(segImgImagePlus);
		try
		{
			output = BatchSegmenter.segment( segImg,
					segmentationModel.selectedSegmenter().get(),
					Intervals.dimensionsAsIntArray( segImg ),
					new StatusServiceProgressWriter( statusService ) );
		}
		catch ( InterruptedException e )
		{
			e.printStackTrace();
		}

	}
}
