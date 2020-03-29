package ai.arcblroth.boss.game.entity;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import ai.arcblroth.boss.engine.Position;
import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.register.EntityBuilder;

public class XulpirBuilder extends EntityBuilder<Xulpir> {

	@Override
	public Xulpir build(Room room, Position pos, JsonObject json) throws JsonParseException {
		return new Xulpir(pos);
	}

}
