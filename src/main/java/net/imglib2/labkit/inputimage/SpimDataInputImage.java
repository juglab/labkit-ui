
package net.imglib2.labkit.inputimage;

import bdv.ViewerSetupImgLoader;
import bdv.spimdata.XmlIoSpimDataMinimal;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.labkit.utils.CheckedExceptionUtils;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.scijava.util.ArrayUtils;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SpimDataInputImage implements InputImage {

	private final AbstractSpimData<?> spimData;

	private final RandomAccessibleInterval<?> imageForSegmentation;

	private final String filename;

	private final boolean timeseries;
	private AbstractSequenceDescription<?, ?, ?> sequence;

	public SpimDataInputImage(String filename) {
		this.spimData = CheckedExceptionUtils.run(() -> new XmlIoSpimDataMinimal()
			.load(filename));
		this.sequence = spimData.getSequenceDescription();
		this.filename = filename;
		this.timeseries = sequence.getTimePoints().size() > 1;
		this.imageForSegmentation = initImageForSegmentation();
	}

	@Override
	public BdvShowable showable() {
		return BdvShowable.wrap(spimData);
	}

	@Override
	public RandomAccessibleInterval<? extends NumericType<?>>
		imageForSegmentation()
	{
		return LabkitUtils.uncheckedCast(imageForSegmentation);
	}

	private RandomAccessibleInterval<?> initImageForSegmentation() {
		BasicViewSetup setup = getSetup();
		ViewerSetupImgLoader<?, ?> imgLoader = (ViewerSetupImgLoader) sequence
			.getImgLoader().getSetupImgLoader(setup.getId());
		List<TimePoint> timePoints = sequence.getTimePoints()
			.getTimePointsOrdered();
		int level = selectResolution(setup.getSize(), imgLoader
			.getMipmapResolutions());
		return combineFrames(imgLoader, timePoints, level);
	}

	private static int selectResolution(Dimensions dimensions,
		double[][] resolutions)
	{
		if (resolutions.length <= 1) return 0;
		Object[] choices = Stream.of(resolutions).map(resolution -> toString(
			dimensions, resolution)).toArray();
		Object choice = JOptionPane.showInputDialog(null,
			"Select Resolution for Segmentation", "Labkit: open Image",
			JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
		if (choice == null) throw new CancellationException();
		return ArrayUtils.indexOf(choices, choice);
	}

	private static String toString(Dimensions dimensions, double[] resolution) {
		long[] fullsize = Intervals.dimensionsAsLongArray(dimensions);
		long[] resolutionSize = IntStream.range(0, fullsize.length).mapToLong(
			i -> (long) (fullsize[i] / resolution[i])).toArray();
		return Arrays.toString(resolutionSize);
	}

	private <T> RandomAccessibleInterval<?> combineFrames(
		ViewerSetupImgLoader<T, ?> imgLoader, List<TimePoint> timePoints, int level)
	{
		if (timePoints.size() == 1) return imgLoader.getImage(timePoints.get(0)
			.getId(), level);
		List<RandomAccessibleInterval<T>> slices = timePoints.stream().map(
			t -> imgLoader.getImage(t.getId(), level)).collect(Collectors.toList());
		return Views.stack(slices);
	}

	@Override
	public int getSpatialDimensions() {
		return interval().numDimensions() - (isTimeSeries() ? 1 : 0);
	}

	@Override
	public String getDefaultLabelingFilename() {
		return filename + ".labeling";
	}

	@Override
	public String getName() {
		return new File(filename).getName();
	}

	@Override
	public List<CalibratedAxis> axes() {
		VoxelDimensions voxelSize = getVoxelDimensions();
		List<CalibratedAxis> list = new ArrayList<>();
		for (int i = 0; i < voxelSize.numDimensions(); i++)
			list.add(new DefaultLinearAxis(voxelSize.dimension(i)));
		if (timeseries) list.add(new DefaultLinearAxis(Axes.TIME));
		return list;
	}

	private VoxelDimensions getVoxelDimensions() {
		BasicViewSetup setup = getSetup();
		if (setup.hasVoxelSize()) return setup.getVoxelSize();
		return defaultVoxelSize();
	}

	private FinalVoxelDimensions defaultVoxelSize() {
		return new FinalVoxelDimensions(null, IntStream.range(0,
			getSpatialDimensions()).mapToDouble(x -> 1.0).toArray());
	}

	private BasicViewSetup getSetup() {
		return sequence.getViewSetupsOrdered().get(0);
	}

	@Override
	public boolean isTimeSeries() {
		return timeseries;
	}
}
