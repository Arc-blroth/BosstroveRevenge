package ai.arcblroth.boss.register;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import com.google.gson.JsonObject;

import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.TilePosition;
import ai.arcblroth.boss.engine.tile.WallTile;
import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.util.TriFunction;

public class WallTileRegistry {
	
	private static final WallTileRegistry INSTANCE = new WallTileRegistry();
	private final ConcurrentHashMap<String, WallTileBuilder<? extends WallTile>> map;
	
	private WallTileRegistry() {
		map = new ConcurrentHashMap<>();
	}
	
	public static WallTileRegistry instance() {
		return INSTANCE;
	}
	
	public WallTile buildTile(String key, Room room, TilePosition position, JsonObject context) {
		return map.get(key).apply(room, position, context);
	}
	
	public WallTile buildTile(String key, Room room, TilePosition position) {
		return map.get(key).apply(room, position, new JsonObject());
	}
	
	public void register(String key, WallTileBuilder builder) {
		map.put(key, builder);
	}
	
	public void register(String key, Texture tileTexture, TriFunction<Room, TilePosition, JsonObject, WallTile> builder) {
		map.put(key, new WallTileBuilder(tileTexture) {
			public WallTile build(Room room, TilePosition tilePos, JsonObject context) {
				return builder.apply(room, tilePos, context);
			}
		});
	}
	
	public void register(String key, Texture tileTexture, BiFunction<Room, TilePosition, WallTile> builder) {
		map.put(key, new WallTileBuilder(tileTexture) {
			public WallTile build(Room room, TilePosition tilePos, JsonObject context) {
				return builder.apply(room, tilePos);
			}
		});
	}

	public void forEach(BiConsumer<String, WallTileBuilder<? extends WallTile>> action) {
		map.forEach(action);
	}
	
	public boolean containsKey(String key) {
		return map.containsKey(key);
	}
	
	public boolean containsValue(WallTileBuilder<? extends WallTile> value) {
		return map.containsValue(value);
	}
	
	public boolean contains(WallTileBuilder<? extends WallTile> value) {
		return map.contains(value);
	}
	
}
