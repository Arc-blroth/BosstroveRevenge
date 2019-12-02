package ai.arcblroth.boss.register;

import java.util.concurrent.ConcurrentHashMap;

import ai.arcblroth.boss.engine.tile.FloorTile;
import ai.arcblroth.boss.engine.tile.WallTile;

public class WallTileRegistry extends ConcurrentHashMap<String, WallTile> {
	
	private static final WallTileRegistry INSTANCE = new WallTileRegistry();
	
	private WallTileRegistry() {
		super();
	}
	
	public static WallTileRegistry get() {
		return INSTANCE;
	}
	
	public static WallTile getTile(String key) {
		return INSTANCE.get(key);
	}
	
	public static void register(String key, WallTile wallTile) {
		INSTANCE.put(key, wallTile);
	}
	
}
