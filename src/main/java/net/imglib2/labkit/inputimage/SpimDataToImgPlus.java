
package net.imglib2.labkit.inputimage;

import bdv.ViewerSetupImgLoader;
import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.TimePoints;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imagej.axis.IdentityAxis;
import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.labkit.utils.CheckedExceptionUtils;
import net.imglib2.util.Cast;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.scijava.util.ArrayUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SpimDataToImgPlus {

	public static ImgPlus<?> openWithGuiForLevelSelection(
		String filename)
	{
		return open(filename, null);
	}

	public static ImgPlus<?> open(String filename, int resolutionLevel) {
		return open(filename, (Integer) resolutionLevel);
	}

	private static ImgPlus<?> open(String filename, Integer resolutionLevel) {
		SpimDataMinimal spimData = CheckedExceptionUtils.run(() -> new XmlIoSpimDataMinimal().load(
			filename));
		return wrap(spimData, resolutionLevel);
	}

	public static ImgPlus<?> wrap(AbstractSpimData<?> spimData, Integer resolutionLevel) {
		Img<?> img = ImgView.wrap(Cast.unchecked(asRai(spimData, resolutionLevel)), null);
		String name = spimData.getSequenceDescription().getViewSetups().get(0).getName();
		CalibratedAxis[] axes = getAxes(spimData);
		return new ImgPlus<>(img, name, axes);
	}

	private static <T> RandomAccessibleInterval<?> asRai(AbstractSpimData<?> spimData,
		Integer level)
	{
		AbstractSequenceDescription<?, ?, ?> sequence = spimData.getSequenceDescription();
		List<? extends BasicViewSetup> viewSetupsOrdered = sequence.getViewSetupsOrdered();
		BasicViewSetup setup = viewSetupsOrdered.get(0);
		List<ViewerSetupImgLoader<T, ?>> imgLoaders = viewSetupsOrdered.stream().map(
			s -> (ViewerSetupImgLoader<T, ?>) sequence.getImgLoader().getSetupImgLoader(s.getId()))
			.collect(Collectors.toList());
		List<TimePoint> timePoints = sequence.getTimePoints().getTimePointsOrdered();
		if (level == null) level = selectResolution(setup.getSize(), imgLoaders.get(0)
			.getMipmapResolutions());
		return dropThirdDimension(combineFrames(imgLoaders, timePoints, level));
	}

	private static RandomAccessibleInterval<?> dropThirdDimension(
		RandomAccessibleInterval<?> image)
	{
		return image.dimension(2) != 1 ? image : Views.hyperSlice(image, 2, 0);
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

	private static <T> RandomAccessibleInterval<T> combineFrames(
		List<ViewerSetupImgLoader<T, ?>> imgLoaders, List<TimePoint> timePoints, int level)
	{
		List<RandomAccessibleInterval<T>> frames = timePoints.stream()
			.map(t -> combineChannels(imgLoaders, t, level))
			.collect(Collectors.toList());
		return frames.size() > 1 ? Views.stack(frames) : frames.get(0);
	}

	private static <T> RandomAccessibleInterval<T> combineChannels(
		List<ViewerSetupImgLoader<T, ?>> imgLoaders, TimePoint timePoint, int level)
	{
		List<RandomAccessibleInterval<T>> channels = imgLoaders.stream()
			.map(imgLoader -> imgLoader.getImage(timePoint.getId(), level))
			.collect(Collectors.toList());
		return channels.size() > 1 ? Views.stack(channels) : channels.get(0);
	}

	public static CalibratedAxis[] getAxes(AbstractSpimData<?> spimData) {
		VoxelDimensions voxelSize = getVoxelDimensions(spimData);
		List<CalibratedAxis> list = new ArrayList<>();
		list.add(new DefaultLinearAxis(Axes.X, voxelSize.unit(), voxelSize.dimension(0)));
		list.add(new DefaultLinearAxis(Axes.Y, voxelSize.unit(), voxelSize.dimension(1)));
		if (is3d(spimData))
			list.add(new DefaultLinearAxis(Axes.Z, voxelSize.unit(), voxelSize.dimension(2)));
		if (isMultiChannel(spimData))
			list.add(new IdentityAxis(Axes.CHANNEL));
		if (isTimeSeries(spimData))
			list.add(new DefaultLinearAxis(Axes.TIME));
		return list.toArray(new CalibratedAxis[0]);
	}

	private static boolean is3d(AbstractSpimData<?> spimData) {
		return spimData.getSequenceDescription().getViewSetupsOrdered().get(0).getSize().dimension(
			2) > 1;
	}

	private static boolean isTimeSeries(AbstractSpimData<?> spimData) {
		TimePoints timePoints = spimData.getSequenceDescription().getTimePoints();
		return timePoints.size() > 1;
	}

	private static boolean isMultiChannel(AbstractSpimData<?> spimData) {
		Map<Integer, ?> setups = spimData.getSequenceDescription().getViewSetups();
		return setups.size() > 1;
	}

	private static VoxelDimensions getVoxelDimensions(AbstractSpimData<?> spimData) {
		BasicViewSetup setup = spimData.getSequenceDescription().getViewSetupsOrdered().get(0);
		if (setup.hasVoxelSize()) return setup.getVoxelSize();
		return new FinalVoxelDimensions("pixel", 1, 1, 1);
	}
}
