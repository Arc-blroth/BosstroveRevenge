package ai.arcblroth.boss.register;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import com.google.gson.JsonObject;

import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.TilePosition;
import ai.arcblroth.boss.engine.tile.FloorTile;
import ai.arcblroth.boss.util.TriFunction;

public class FloorTileRegistry extends ConcurrentHashMap<String, FloorTileBuilder> {
	
	private static final FloorTileRegistry INSTANCE = new FloorTileRegistry();
	
	private FloorTileRegistry() {
		super();
	}
	
	public static FloorTileRegistry instance() {
		return INSTANCE;
	}
	
	public FloorTile buildTile(String key, Room room, TilePosition position, JsonObject context) {
		return get(key).apply(room, position, context);
	}
	
	public FloorTile buildTile(String key, Room room, TilePosition position) {
		return get(key).apply(room, position, new JsonObject());
	}
	
	public void register(String key, FloorTileBuilder builder) {
		put(key, builder);
	}
	
	public void register(String key, TriFunction<Room, TilePosition, JsonObject, FloorTile> builder) {
		put(key, new FloorTileBuilder() {
			public FloorTile build(Room room, TilePosition tilePos, JsonObject context) {
				return builder.apply(room, tilePos, context);
			}
		});
	}
	
	public void register(String key, BiFunction<Room, TilePosition, FloorTile> builder) {
		put(key, new FloorTileBuilder() {
			public FloorTile build(Room room, TilePosition tilePos, JsonObject context) {
				return builder.apply(room, tilePos);
			}
		});
	}
	
}
