package ai.arcblroth.boss.register;

import java.util.concurrent.ConcurrentHashMap;

import ai.arcblroth.boss.engine.tile.FloorTile;
import ai.arcblroth.boss.engine.tile.ITile;

public class FloorTileRegistry<T extends FloorTile> extends ConcurrentHashMap<String, T> {
	
	private static final FloorTileRegistry<? extends FloorTile> INSTANCE = new FloorTileRegistry<>();
	
	private FloorTileRegistry() {
		super();
	}
	
	public static FloorTileRegistry<? extends FloorTile> get() {
		return INSTANCE;
	}
	
	public static FloorTile getTile(String key) {
		return INSTANCE.get(key);
	}
	
}
