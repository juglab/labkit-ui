package net.imglib2.labkit;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * @author Matthias Arzt
 */
// TODO : Refactor FeatureStack, what it is actually used for, can it be remove / replaced by something more appropriate
public class FeatureStack {

	private RandomAccessibleInterval<?> original;

	private final double scaling;

	private final CellGrid grid;

	private final RandomAccessibleInterval<?> preparedOriginal;

	public FeatureStack(RandomAccessibleInterval<?> original, double scaling, boolean isTimeSeries) {
		this.original = original;
		this.scaling = scaling;
		this.grid = LabkitUtils.suggestGrid(original, isTimeSeries);
		this.preparedOriginal = prepareOriginal(original);
	}

	private RandomAccessibleInterval<?> prepareOriginal(RandomAccessibleInterval<?> original) {
		Object voxel = original.randomAccess().get();
		if(voxel instanceof RealType && !(voxel instanceof FloatType))
			return LabkitUtils.toFloat(RevampUtils.uncheckedCast(original));
		return original;
	}

	public double scaling() {
		return scaling;
	}

	public Interval interval() {
		return new FinalInterval(original);
	}

	public CellGrid grid() {
		return grid;
	}

	public RandomAccessibleInterval<?> compatibleOriginal() {
		return preparedOriginal;
	}
}
