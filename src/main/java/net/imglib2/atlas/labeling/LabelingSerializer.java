package net.imglib2.atlas.labeling;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.imglib2.Interval;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.logic.BitType;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author Matthias Arzt
 */
public class LabelingSerializer
{
	public static Labeling load(String filename) throws IOException {
		try(FileReader reader = new FileReader(filename)) {
			return new Gson().fromJson(reader, Labeling.class);
		}
	}

	public static void save(Labeling roi, String filename) throws IOException {
		try(FileWriter writer = new FileWriter(filename)) {
			new Gson().toJson(writer, Labeling.class);
		}
	}

	public static class Adapter extends TypeAdapter<Labeling> {

		SparseRoiSerializer.Adapter sparseRoiAdapter = new SparseRoiSerializer.Adapter();

		private static final Type regionType = new TypeToken<IterableRegion<BitType>>(){}.getType();

		private static final Type mapType = new TypeToken<Map<String, IterableRegion<BitType>>>(){}.getType();

		@Override
		public void write(JsonWriter jsonWriter, Labeling labeling) throws IOException {
			writeRegions(jsonWriter, labeling.regions());
		}

		private void writeRegions(JsonWriter jsonWriter, Map<String, IterableRegion<BitType>> regions) throws IOException {
			Gson gson = new GsonBuilder().registerTypeAdapter(regionType, new SparseRoiSerializer.Serializer()).create();
			gson.toJson(regions, mapType, jsonWriter);
		}

		@Override
		public Labeling read(JsonReader jsonReader) throws IOException {
			Map<String, IterableRegion<BitType>> regions = readRegions(jsonReader);
			Interval interval = regions.values().stream().findAny().get();
			return new Labeling(regions, interval);
		}

		private Map<String,IterableRegion<BitType>> readRegions(JsonReader jsonReader) {
			Gson gson = new GsonBuilder().registerTypeAdapter(regionType, new SparseRoiSerializer.Deserializer()).create();
			return gson.fromJson(jsonReader, mapType);
		}
	}
}
