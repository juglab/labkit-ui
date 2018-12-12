
package net.imglib2.labkit.labeling;

import clojure.lang.Obj;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.AbstractWrappedInterval;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.sparse.DifferenceRandomAccessibleIntType;
import net.imglib2.sparse.SparseRandomAccessIntType;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DifferenceLabeling extends AbstractWrappedInterval<Interval>
	implements Labeling
{

	private final Labeling source;
	private final RandomAccessibleInterval<? extends IntegerType<?>> base;
	private final DifferenceRandomAccessibleIntType difference;

	public DifferenceLabeling(
		ImgLabeling<String, ? extends IntegerType<?>> input)
	{
		super(new FinalInterval(input));
		difference = new DifferenceRandomAccessibleIntType(input.getIndexImg());
		final List<Set<String>> labelSets = Labelings.getLabelSets(input
			.getMapping());
		ImgLabeling<String, ?> labeling = LabelingSerializer.fromImageAndLabelSets(
			difference, labelSets);
		source = Labeling.fromImgLabeling(labeling);
	}

	public static class DifferenceString {

		public final AddRemove action;
		public final String label;

		public DifferenceString(AddRemove action, String label) {
			this.action = action;
			this.label = label;
		}

		public static DifferenceString add(String label) {
			return new DifferenceString(AddRemove.ADD, label);
		}

		public static DifferenceString remove(String label) {
			return new DifferenceString(AddRemove.REMOVE, label);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof DifferenceString)) return false;
			DifferenceString that = (DifferenceString) obj;
			return (action == that.action) && label.equals(that.label);
		}

		@Override
		public int hashCode() {
			return 31 * action.hashCode() + label.hashCode();
		}
	}

	public enum AddRemove {
			ADD, REMOVE
	}

	public ImgLabeling<DifferenceString, ?> difference() {
		SparseRandomAccessIntType img = new SparseRandomAccessIntType(interval());
		ImgLabeling<DifferenceString, ?> result = new ImgLabeling<>(img);
		Cursor<Void> cursor = difference.differencePattern().cursor();
		RandomAccess<? extends IntegerType<?>> old = base.randomAccess();
		RandomAccess<? extends IntegerType> newer = difference.randomAccess();
		RandomAccess<LabelingType<DifferenceString>> resultCursor = result
			.randomAccess();
		List<Set<Label>> labelsets = source.getLabelSets();
		while (cursor.hasNext()) {
			cursor.fwd();
			old.setPosition(cursor);
			newer.setPosition(cursor);
			resultCursor.setPosition(cursor);
			Set<Label> oldLabelSet = labelsets.get(old.get().getInteger());
			Set<Label> newLabelSet = labelsets.get(old.get().getInteger());
			LabelingType<DifferenceString> diff = resultCursor.get();
			writeDifferenceSet(oldLabelSet, newLabelSet, diff);
		}
		return result;
	}

	public void applyDifference(ImgLabeling<DifferenceString, ?> difference) {
		Cursor<IntType> cursor = getSparsityCursor(difference);
		RandomAccess<LabelingType<DifferenceString>> changesRandomAccess =
			difference.randomAccess();
		RandomAccess<Set<Label>> randomAccess = randomAccess();
		while (cursor.hasNext()) {
			cursor.fwd();
			changesRandomAccess.setPosition(cursor);
			randomAccess.setPosition(cursor);
			RandomAccess<LabelingType<DifferenceString>> changes =
				changesRandomAccess;
			Set<Label> pixel = randomAccess.get();
			for (DifferenceString change : changes.get())
				switch (change.action) {
					case ADD:
						pixel.add(addLabel(change.label));
						break;
					case REMOVE:
						pixel.remove(getLabel(change.label));
						break;
				}
		}
	}

	private static Cursor<IntType> getSparsityCursor(
		ImgLabeling<DifferenceString, ?> difference)
	{
		RandomAccessibleInterval<?> indexImg = difference.getIndexImg();
		if (indexImg instanceof SparseRandomAccessIntType)
			return ((SparseRandomAccessIntType) indexImg).sparseCursor();
		throw new UnsupportedOperationException();
	}

	private void writeDifferenceSet(Set<Label> oldLabelSet,
		Set<Label> newLabelSet, LabelingType<DifferenceString> diff)
	{
		for (Label label : oldLabelSet)
			if (!newLabelSet.contains(label)) diff.add(DifferenceString.remove(label
				.name()));
		for (Label label : newLabelSet)
			if (!oldLabelSet.contains(label)) diff.add(DifferenceString.add(label
				.name()));
	}

	@Override
	public Interval interval() {
		return source.interval();
	}

	@Override
	public List<Label> getLabels() {
		return source.getLabels();
	}

	@Override
	public void setAxes(List<CalibratedAxis> axes) {
		source.setAxes(axes);
	}

	@Override
	public Label getLabel(String name) {
		return source.getLabel(name);
	}

	@Override
	public RandomAccessibleInterval<BitType> getRegion(Label label) {
		return source.getRegion(label);
	}

	@Override
	public Map<Label, IterableRegion<BitType>> iterableRegions() {
		return source.iterableRegions();
	}

	@Override
	public Cursor<?> sparsityCursor() {
		return source.sparsityCursor();
	}

	@Override
	public RandomAccessibleInterval<? extends IntegerType<?>> getIndexImg() {
		return source.getIndexImg();
	}

	@Override
	public List<Set<Label>> getLabelSets() {
		return source.getLabelSets();
	}

	@Override
	public List<CalibratedAxis> axes() {
		return source.axes();
	}

	@Override
	public Label addLabel(String label) {
		return source.addLabel(label);
	}

	@Override
	public void addLabel(String newName,
		RandomAccessibleInterval<? extends BooleanType<?>> bitmap)
	{
		source.addLabel(newName, bitmap);
	}

	@Override
	public void removeLabel(Label label) {
		source.removeLabel(label);
	}

	@Override
	public void renameLabel(Label oldLabel, String newLabel) {
		source.renameLabel(oldLabel, newLabel);
	}

	@Override
	public void clearLabel(Label label) {
		source.clearLabel(label);
	}

	@Override
	public void setLabelOrder(Comparator<? super Label> comparator) {
		source.setLabelOrder(comparator);
	}

	@Override
	public RandomAccess<Set<Label>> randomAccess() {
		return source.randomAccess();
	}

	@Override
	public RandomAccess<Set<Label>> randomAccess(Interval interval) {
		return source.randomAccess(interval);
	}
}
