package net.imglib2.atlas.labeling;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.scif.services.DatasetIOService;
import net.imagej.DatasetService;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.atlas.AtlasUtils;
import net.imglib2.img.Img;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import org.apache.commons.io.FilenameUtils;
import org.scijava.Context;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Matthias Arzt
 */
public class LabelingSerializer
{
	private final Context context;

	public LabelingSerializer(Context context) {
		this.context = context;
	}

	public Labeling load(String filename) throws IOException {
		if(FilenameUtils.isExtension(filename, new String[]{"tif", "tiff"}))
			return loadFromTiff(filename);
		if(FilenameUtils.isExtension(filename, "json"))
			return loadFromJson(filename);
		throw new IllegalArgumentException("Filename must have supported extension (*.json, *.tif, *.tiff)");
	}

	private Labeling loadFromJson(String filename) throws IOException {
		try(FileReader reader = new FileReader(filename)) {
			return new Gson().fromJson(reader, Labeling.class);
		}
	}

	private Labeling loadFromTiff(String filename) throws IOException {
		Img<? extends IntegerType<?>> img = loadImageFromTiff(filename);
		LabelsMetaData meta = (new File(filename + ".labels").exists()) ?
				loadMetaData(filename + ".labels") :
				new LabelsMetaData(img);
		return new Labeling(fromImageAndLabelSets(img, meta.asLabelSets()));
	}

	private ImgLabeling<String,?> fromImageAndLabelSets(Img<? extends IntegerType<?>> img, List<Set<String>> labelSets) {
		ImgLabeling<String, ?> result = new ImgLabeling<>(AtlasUtils.uncheckedCast(img));
		new LabelingMapping.SerialisationAccess<String>(result.getMapping()){
			public void run() {
				setLabelSets(labelSets);
			}
		}.run();
		return result;
	}

	private Img<? extends IntegerType<?>> loadImageFromTiff(String filename) throws IOException {
		DatasetIOService io = context.service(DatasetIOService.class);
		return AtlasUtils.uncheckedCast(io.open(filename).getImgPlus().getImg());
	}

	private LabelsMetaData loadMetaData(String filename) throws IOException {
		try(FileReader reader = new FileReader(filename)) {
			return new Gson().fromJson(reader, LabelsMetaData.class);
		}
	}

	public void save(Labeling labeling, String filename) throws IOException {
		if(FilenameUtils.isExtension(filename, new String[]{"tif", "tiff"}))
			saveAsTiff(labeling, filename);
		else if(FilenameUtils.isExtension(filename, "json"))
			saveAsJson(labeling, filename);
		else throw new UnsupportedOperationException();
	}

	private void saveAsJson(Labeling labeling, String filename) throws IOException {
		try(FileWriter writer = new FileWriter(filename)) {
			new Gson().toJson(labeling, Labeling.class, writer);
		}
	}

	private <I extends IntegerType<I>> void saveAsTiff(Labeling labeling, String filename) throws IOException {
		LabelsMetaData meta = new LabelsMetaData(labeling.getLabelSets());
		try(FileWriter writer = new FileWriter(filename + ".labels")) {
			new Gson().toJson(meta, writer);
		}
		DatasetIOService io = context.service(DatasetIOService.class);
		DatasetService ds = context.service(DatasetService.class);
		RandomAccessibleInterval<I> imgPlus = AtlasUtils.uncheckedCast(labeling.getIndexImg());
		io.save(ds.create(imgPlus), filename);
	}

	private static class LabelsMetaData {
		List<Set<String>> labelSets;

		public LabelsMetaData(Img<? extends IntegerType<?>> img) {
			IntType max = new IntType(0);
			img.forEach(x -> { if(max.get() < x.getInteger()) max.set(x.getInteger()); });
			labelSets = IntStream.rangeClosed(0, max.get())
					.mapToObj(i -> i == 0 ? Collections.<String>emptySet() : Collections.singleton(Integer.toString(i)))
					.collect(Collectors.toList());
		}

		public LabelsMetaData(List<Set<String>> mapping) {
			labelSets = mapping.stream().map(TreeSet::new).collect(Collectors.toList());
		}

		public List<Set<String>> asLabelSets() {
			return labelSets;
		}
	}

	public static class Adapter extends TypeAdapter<Labeling> {

		private static final Type regionType = new TypeToken<IterableRegion<BitType>>(){}.getType();

		private static final Type mapType = new TypeToken<Map<String, IterableRegion<BitType>>>(){}.getType();

		@Override
		public void write(JsonWriter jsonWriter, Labeling labeling) throws IOException {
			writeRegions(jsonWriter, labeling.iterableRegions());
		}

		private void writeRegions(JsonWriter jsonWriter, Map<String, ? extends IterableRegion<BitType>> regions) throws IOException {
			Gson gson = new GsonBuilder().registerTypeAdapter(regionType, new SparseIterableRegionSerializer.Serializer()).create();
			gson.toJson(regions, mapType, jsonWriter);
		}

		@Override
		public Labeling read(JsonReader jsonReader) throws IOException {
			Map<String, IterableRegion<BitType>> regions = readRegions(jsonReader);
			Interval interval = regions.values().stream().findAny().get();
			return new Labeling(regions, interval);
		}

		private Map<String,IterableRegion<BitType>> readRegions(JsonReader jsonReader) {
			Gson gson = new GsonBuilder().registerTypeAdapter(regionType, new SparseIterableRegionSerializer.Deserializer()).create();
			return gson.fromJson(jsonReader, mapType);
		}
	}
}
