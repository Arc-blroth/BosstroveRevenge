package ai.arcblroth.boss.game.entity;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import ai.arcblroth.boss.engine.Position;

public class XulpirDeserializer implements JsonDeserializer<Xulpir> {

	@Override
	public Xulpir deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject ent = json.getAsJsonObject();
		
		Position p = new Position(ent.get("x").getAsDouble(), ent.get("y").getAsDouble());
		return new Xulpir(p);
	}

}
