
package net.imglib2.labkit.labeling;

import com.google.gson.annotations.JsonAdapter;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.IntegerType;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonAdapter(LabelingSerializer.Adapter.class)
public interface Labeling extends Interval,
	RandomAccessibleInterval<Set<Label>>
{

	static Labeling createEmpty(List<String> labels, Interval interval) {
		return DefaultLabeling.createEmpty(labels, interval);
	}

	static Labeling createEmptyLabels(List<Label> labels, Interval interval) {
		return DefaultLabeling.createEmptyLabels(labels, interval);
	}

	static Labeling fromImgLabeling(ImgLabeling<String, ?> imgLabeling) {
		return DefaultLabeling.fromImgLabeling(imgLabeling);
	}

	static Labeling fromMap(Map<String, IterableRegion<BitType>> regions) {
		return DefaultLabeling.fromMap(regions);
	}

	Interval interval();

	List<Label> getLabels();

	void setAxes(List<CalibratedAxis> axes);

	Label getLabel(String name);

	RandomAccessibleInterval<BitType> getRegion(Label label);

	Map<Label, IterableRegion<BitType>> iterableRegions();

	Cursor<?> sparsityCursor();

	RandomAccessibleInterval<? extends IntegerType<?>> getIndexImg();

	List<Set<Label>> getLabelSets();

	List<CalibratedAxis> axes();

	Label addLabel(String label);

	void addLabel(String newName,
		RandomAccessibleInterval<? extends BooleanType<?>> bitmap);

	void removeLabel(Label label);

	void renameLabel(Label oldLabel, String newLabel);

	void clearLabel(Label label);

	void setLabelOrder(Comparator<? super Label> comparator);

	@Override
	RandomAccess<Set<Label>> randomAccess();

	@Override
	RandomAccess<Set<Label>> randomAccess(Interval interval);
}
