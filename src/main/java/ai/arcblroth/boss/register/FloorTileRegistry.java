package ai.arcblroth.boss.register;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import com.google.gson.JsonObject;

import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.TilePosition;
import ai.arcblroth.boss.engine.tile.FloorTile;
import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.util.TriFunction;

public class FloorTileRegistry {
	
	private static final FloorTileRegistry INSTANCE = new FloorTileRegistry();
	private final ConcurrentHashMap<String, FloorTileBuilder<? extends FloorTile>> map;
	
	private FloorTileRegistry() {
		map = new ConcurrentHashMap<>();
	}
	
	public static FloorTileRegistry instance() {
		return INSTANCE;
	}
	
	public FloorTile buildTile(String key, Room room, TilePosition position, JsonObject context) {
		return map.get(key).apply(room, position, context);
	}
	
	public FloorTile buildTile(String key, Room room, TilePosition position) {
		return map.get(key).apply(room, position, new JsonObject());
	}
	
	public void register(String key, FloorTileBuilder builder) {
		map.put(key, builder);
	}
	
	public void register(String key, Texture tileTexture, TriFunction<Room, TilePosition, JsonObject, FloorTile> builder) {
		map.put(key, new FloorTileBuilder(tileTexture) {
			public FloorTile build(Room room, TilePosition tilePos, JsonObject context) {
				return builder.apply(room, tilePos, context);
			}
		});
	}
	
	public void register(String key, Texture tileTexture, BiFunction<Room, TilePosition, FloorTile> builder) {
		map.put(key, new FloorTileBuilder(tileTexture) {
			public FloorTile build(Room room, TilePosition tilePos, JsonObject context) {
				return builder.apply(room, tilePos);
			}
		});
	}

	public void forEach(BiConsumer<String, FloorTileBuilder<? extends FloorTile>> action) {
		map.forEach(action);
	}
	
	public boolean containsKey(String key) {
		return map.containsKey(key);
	}
	
	public boolean containsValue(FloorTileBuilder<? extends FloorTile> value) {
		return map.containsValue(value);
	}
	
	public boolean contains(FloorTileBuilder<? extends FloorTile> value) {
		return map.contains(value);
	}
	
}
