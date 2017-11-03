package net.imglib2.atlas.plugin;

import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.LinearAxis;
import net.imagej.table.Column;
import net.imagej.table.DefaultColumn;
import net.imagej.table.DefaultGenericTable;
import net.imagej.table.DoubleColumn;
import net.imagej.table.GenericTable;
import net.imagej.table.Table;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.fill.Filter;
import net.imglib2.algorithm.fill.FloodFill;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.atlas.labeling.Labeling;
import net.imglib2.atlas.labeling.LabelingSerializer;
import net.imglib2.roi.IterableRegion;
import net.imglib2.sparse.SparseRandomAccessIntType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Pair;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Plugin(type = Command.class, menuPath = "Plugins > Segmentation > Measure Connected Components")
public class MeasureConnectedComponents implements Command {

	@Parameter
	Context context;

	@Parameter(label = "File containing labeling (*.labeling)")
	File labelingFile;

	@Parameter(label = "calculate calibrated size")
	boolean calibratedSize = true;

	@Parameter(type = ItemIO.OUTPUT)
	Table<?, ?> table;

	@Override
	public void run() {
		try {
			Labeling labeling = new LabelingSerializer(context).open(labelingFile.getAbsolutePath());
			TableBuilder builder = new TableBuilder(labeling.axes());
			labeling.iterableRegions().forEach((label, mask) -> builder.add(label, connectedComponets(mask)));
			table = builder.getTable();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static List<Long> connectedComponets(IterableRegion<BitType> region) {
		List<Long> sizes = new ArrayList<>();
		Cursor<Void> cursor = region.cursor();
		SparseRandomAccessIntType visitedImage = new SparseRandomAccessIntType(region);
		RandomAccess<IntType> visited = visitedImage.randomAccess();
		int currentIndex = 0;
		while(cursor.hasNext()) {
			cursor.fwd();
			visited.setPosition(cursor);
			if(visited.get().get() == 0) {
				currentIndex++;
				long countBefore = visitedImage.sparsityPattern().size();
				Filter<Pair<BitType, IntType>, Pair<BitType, IntType>> filter = (current, seed) -> current.getA().get() && current.getB().get() == 0;
				FloodFill.fill(region, visitedImage, cursor, new IntType(currentIndex), new DiamondShape(1), filter);
				long countAfter = visitedImage.sparsityPattern().size();
				sizes.add(countAfter - countBefore);
			}
		}
		return sizes;
	}

	private class TableBuilder {

		Column<String> labels = new DefaultColumn<>(String.class, "label");
		Column<Integer> indices = new DefaultColumn<>(Integer.class, "connect component");
		Column<Long> number = new DefaultColumn<Long>(Long.class, "size in pixels");
		Column<Double> sizes = new DoubleColumn("size");

		private final double pixelSize;

		public TableBuilder(List<CalibratedAxis> axes) {
			double pixelSize = 1;
			StringJoiner unit = new StringJoiner("*");
			boolean unknown = false;
			for(CalibratedAxis axis : axes)
				if(axis instanceof LinearAxis) {
					LinearAxis linear = (LinearAxis) axis;
					pixelSize *= linear.scale();
					unit.add(linear.unit());
				}
				else
					unknown = true;
			this.pixelSize = unknown ? 1 : pixelSize;
			sizes.setHeader(sizes.getHeader() + " in " + (unknown ? "unknown" : unit.toString()));
		}

		public void add(String label, List<Long> sizesInPixels) {
			int index = 0;
			for(Long size : sizesInPixels) {
				index++;
				labels.add(label);
				indices.add(index);
				number.add(size);
				sizes.add(size * pixelSize);
			}
		}

		public Table<?, ?> getTable() {
			GenericTable table = new DefaultGenericTable();
			table.add(labels);
			table.add(indices);
			table.add(number);
			if(calibratedSize)
				table.add(sizes);
			return table;
		}
	}
}
