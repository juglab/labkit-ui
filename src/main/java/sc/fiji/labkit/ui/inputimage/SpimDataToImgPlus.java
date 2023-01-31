/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.ui.inputimage;

import bdv.ViewerSetupImgLoader;
import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.registration.ViewRegistrations;
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
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Cast;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.scijava.util.ArrayUtils;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Converts {@link AbstractSpimData} to {@link ImgPlus}.
 * <p>
 * Helper to {@link SpimDataInputImage}.
 */
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
		try {
			SpimDataMinimal spimData = new XmlIoSpimDataMinimal().load(filename);
			return wrap(spimData, resolutionLevel);
		}
		catch (SpimDataException e) {
			throw new RuntimeException(e);
		}
	}

	public static ImgPlus<?> wrap(AbstractSpimData<?> spimData, Integer resolutionLevel) {
		checkSetupsMatch(spimData);
		Img<?> img = ImgView.wrap(Cast.unchecked(asRai(spimData, resolutionLevel)),
			Cast.unchecked(new CellImgFactory()));
		String name = getName(spimData);
		CalibratedAxis[] axes = getAxes(spimData);
		return new ImgPlus<>(img, name, axes);
	}

	private static String getName(AbstractSpimData<?> spimData) {
		AbstractSequenceDescription<?, ?, ?> sequenceDescription =
			spimData.getSequenceDescription();
		int setupId = sequenceDescription.getViewSetupsOrdered().get(0).getId();
		return sequenceDescription.getViewSetups().get(setupId).getName();
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

	private static CalibratedAxis[] getAxes(AbstractSpimData<?> spimData) {
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

	/**
	 * @throws SpimDataInputException if the dataset contains different angles and
	 *           if the image size differ between setups.
	 */
	private static void checkSetupsMatch(AbstractSpimData<?> spimData) {
		List<? extends BasicViewSetup> setups = spimData.getSequenceDescription()
			.getViewSetupsOrdered();
		if (setups.size() == 1)
			return;
		List<Dimensions> setupSizes = setups.stream()
			.map(BasicViewSetup::getSize)
			.collect(Collectors.toList());
		boolean sameSizes = allEqual(setupSizes);
		boolean sameRegistrations = viewRegistrationsMatch(spimData);
		if (!sameSizes || !sameRegistrations)
			throw new SpimDataInputException(
				"The image can not be processed because it contains multiple views / angles." +
					"\nLabkit only supports Big Data Viewer XML + HDF5 files with a single view / angle / setup." +
					"\nYou may use BigStitcher to merge the multiple views into one image before opening it with Labkit.");
	}

	private static boolean viewRegistrationsMatch(AbstractSpimData<?> spimData) {
		List<AffineTransform3D> transformations = getFirstTimePointViewRegistrations(spimData);
		return allEqual(transformations, SpimDataToImgPlus::transformationEquals);
	}

	private static List<AffineTransform3D> getFirstTimePointViewRegistrations(
		AbstractSpimData<?> spimData)
	{
		AbstractSequenceDescription<?, ?, ?> sequenceDescription = spimData.getSequenceDescription();
		int firstTimePoint = sequenceDescription.getTimePoints().getTimePointsOrdered().get(0).getId();
		ViewRegistrations viewRegistrations = spimData.getViewRegistrations();
		List<AffineTransform3D> transformations = new ArrayList<>();
		for (BasicViewSetup setup : sequenceDescription.getViewSetupsOrdered()) {
			AffineTransform3D transform = viewRegistrations.getViewRegistration(firstTimePoint, setup
				.getId()).getModel();
			transformations.add(transform);
		}
		return transformations;
	}

	/** @return true, if all entries in the list are equal. */
	private static <T> boolean allEqual(List<T> values) {
		return allEqual(values, Objects::equals);
	}

	/**
	 * @return true, if all entries in the list are equal. Use the given function
	 *         for comparison.
	 */
	private static <T> boolean allEqual(List<T> values, BiPredicate<T, T> equals) {
		if (values.isEmpty())
			return true;
		T first = values.get(0);
		for (int i = 1; i < values.size(); i++) {
			T value = values.get(i);
			if (!equals.test(first, value))
				return false;
		}
		return true;
	}

	/**
	 * @return True, if the two transformations are equal. Allows a small error
	 *         margin.
	 */
	static boolean transformationEquals(AffineTransform3D a, AffineTransform3D b) {
		double max_abs_value = 0;
		double max_difference = 0;
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 4; col++) {
				double va = a.get(row, col);
				double vb = b.get(row, col);
				max_abs_value = Math.max(max_abs_value, Math.abs(va));
				max_abs_value = Math.max(max_abs_value, Math.abs(vb));
				max_difference = Math.max(max_difference, Math.abs(va - vb));
			}
		}
		return max_difference == 0.0 | max_difference < max_abs_value * 1e-6;
	}
}
