package ai.arcblroth.boss.register;

import java.util.concurrent.ConcurrentHashMap;

import ai.arcblroth.boss.engine.tile.EmptyFloorTile;
import ai.arcblroth.boss.engine.tile.FloorTile;
import ai.arcblroth.boss.engine.tile.ITile;

public class FloorTileRegistry extends ConcurrentHashMap<String, FloorTile> {
	
	private static final FloorTileRegistry INSTANCE = new FloorTileRegistry();
	
	private FloorTileRegistry() {
		super();
	}
	
	public static FloorTileRegistry get() {
		return INSTANCE;
	}
	
	public static FloorTile getTile(String key) {
		return INSTANCE.get(key);
	}

	public static void register(String key, FloorTile floorTile) {
		INSTANCE.put(key, floorTile);
	}
	
}
