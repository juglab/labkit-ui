package net.imglib2.atlas.labeling;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.logic.BitType;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author Matthias Arzt
 */
public class SparseRoiSerializer {

	public static class Adapter extends TypeAdapter<IterableRegion<BitType>> {

		@Override
		public void write(JsonWriter writer, IterableRegion<BitType> roi) throws IOException {
			Type type = new TypeToken<IterableRegion<BitType>>(){}.getType();
			Gson gson = new GsonBuilder().registerTypeAdapter(type, new Serializer()).create();
			gson.toJson(roi, type, writer);
		}

		@Override
		public SparseRoi read(JsonReader reader) throws IOException {
			Gson gson = new GsonBuilder().registerTypeAdapter(SparseRoi.class, new Deserializer()).create();
			return gson.fromJson(reader, SparseRoi.class);
		}
	}

	public static class Serializer implements JsonSerializer<IterableRegion<BitType>> {

		@Override
		public JsonElement serialize(IterableRegion<BitType> input, Type type, JsonSerializationContext context) {
			JsonObject json = new JsonObject();
			json.add("interval", context.serialize(new FinalInterval(input), FinalInterval.class));
			json.add("coordinates", toJson(input.localizingCursor(), context));
			return json;
		}

		private JsonElement toJson(Cursor<Void> cursor, JsonSerializationContext context) {
			JsonArray json = new JsonArray();
			long[] position = new long[cursor.numDimensions()];
			while(cursor.hasNext()) {
				cursor.fwd();
				cursor.localize(position);
				json.add(context.serialize(position, long[].class));
			}
			return json;
		}
	}

	public static class Deserializer implements JsonDeserializer<SparseRoi> {

		@Override
		public SparseRoi deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
			JsonObject json = jsonElement.getAsJsonObject();
			FinalInterval interval = context.deserialize(json.get("interval"), FinalInterval.class);
			SparseRoi roi = new SparseRoi(interval);
			fromJson(roi, json.get("coordinates").getAsJsonArray(), context);
			return roi;
		}

		private void fromJson(SparseRoi roi, JsonArray array, JsonDeserializationContext context) {
			RandomAccess<BitType> ra = roi.randomAccess();
			for(JsonElement element : array) {
				long[] position = context.deserialize(element, long[].class);
				ra.setPosition(position);
				ra.get().set(true);
			}
		}
	}
}
