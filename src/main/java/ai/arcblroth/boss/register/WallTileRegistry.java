package ai.arcblroth.boss.register;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import com.google.gson.JsonObject;

import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.TilePosition;
import ai.arcblroth.boss.engine.tile.WallTile;
import ai.arcblroth.boss.util.TriFunction;

public class WallTileRegistry extends ConcurrentHashMap<String, WallTileBuilder> {
	
	private static final WallTileRegistry INSTANCE = new WallTileRegistry();
	
	private WallTileRegistry() {
		super();
	}
	
	public static WallTileRegistry instance() {
		return INSTANCE;
	}
	
	public WallTile buildTile(String key, Room room, TilePosition position, JsonObject context) {
		return get(key).apply(room, position, context);
	}
	
	public WallTile buildTile(String key, Room room, TilePosition position) {
		return get(key).apply(room, position, new JsonObject());
	}
	
	public void register(String key, WallTileBuilder builder) {
		put(key, builder);
	}
	
	public void register(String key, TriFunction<Room, TilePosition, JsonObject, WallTile> builder) {
		put(key, new WallTileBuilder() {
			public WallTile build(Room room, TilePosition tilePos, JsonObject context) {
				return builder.apply(room, tilePos, context);
			}
		});
	}
	
	public void register(String key, BiFunction<Room, TilePosition, WallTile> builder) {
		put(key, new WallTileBuilder() {
			public WallTile build(Room room, TilePosition tilePos, JsonObject context) {
				return builder.apply(room, tilePos);
			}
		});
	}
	
}
