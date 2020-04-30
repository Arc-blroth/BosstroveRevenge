package ai.arcblroth.boss.register;

import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.area.Area;
import ai.arcblroth.boss.engine.hitbox.Hitbox;
import ai.arcblroth.boss.util.TriFunction;
import com.google.gson.JsonObject;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class AreaRegistry {

	private static final AreaRegistry INSTANCE = new AreaRegistry();
	private final ConcurrentHashMap<String, TriFunction<Room, Hitbox, JsonObject, Area>> map;

	private AreaRegistry() {
		map = new ConcurrentHashMap<>();
	}

	public static AreaRegistry instance() {
		return INSTANCE;
	}

	public Area buildArea(String areaId, Room room, Hitbox hitbox, JsonObject context) {
		return map.get(areaId).apply(room, hitbox, context);
	}

	public void register(String key, TriFunction<Room, Hitbox, JsonObject, Area> entityDef) {
		map.put(key, entityDef);
	}

	public void forEach(BiConsumer<String, TriFunction<Room, Hitbox, JsonObject, Area>> action) {
		map.forEach(action);
	}

	public boolean containsKey(String key) {
		return map.containsKey(key);
	}

	public boolean containsValue(TriFunction<Room, Hitbox, JsonObject, Area> value) {
		return map.containsValue(value);
	}

	public boolean contains(TriFunction<Room, Hitbox, JsonObject, Area> value) {
		return map.contains(value);
	}

}
