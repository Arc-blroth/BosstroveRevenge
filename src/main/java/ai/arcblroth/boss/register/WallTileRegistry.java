package ai.arcblroth.boss.register;

import java.util.concurrent.ConcurrentHashMap;

import ai.arcblroth.boss.engine.tile.FloorTile;
import ai.arcblroth.boss.engine.tile.WallTile;

public class WallTileRegistry extends ConcurrentHashMap<String, WallTile> implements IRegistry<String, WallTile> {
	
	private static final WallTileRegistry INSTANCE = new WallTileRegistry();
	
	private WallTileRegistry() {
		super();
	}
	
	public static WallTileRegistry instance() {
		return INSTANCE;
	}
	
	public WallTile getTile(String key) {
		return get(key);
	}

	@Override
	public WallTile getRegistered(String key) {
		return get(key);
	}

	@Override
	public void register(String key, WallTile wallTile) {
		put(key, wallTile);
	}
	
}
