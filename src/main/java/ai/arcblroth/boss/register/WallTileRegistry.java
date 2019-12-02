package ai.arcblroth.boss.register;

import java.util.concurrent.ConcurrentHashMap;

import ai.arcblroth.boss.engine.tile.WallTile;

public class WallTileRegistry<T extends WallTile> extends ConcurrentHashMap<String, T> {
	
	private static final WallTileRegistry<? extends WallTile> INSTANCE = new WallTileRegistry<>();
	
	private WallTileRegistry() {
		super();
	}
	
	public static WallTileRegistry<? extends WallTile> get() {
		return INSTANCE;
	}
	
	public static WallTile getTile(String key) {
		return INSTANCE.get(key);
	}
	
}
