package ai.arcblroth.boss.register;

import java.util.concurrent.ConcurrentHashMap;

import ai.arcblroth.boss.engine.tile.EmptyFloorTile;
import ai.arcblroth.boss.engine.tile.FloorTile;
import ai.arcblroth.boss.engine.tile.ITile;

public class FloorTileRegistry extends ConcurrentHashMap<String, FloorTile> implements IRegistry<String, FloorTile> {
	
	private static final FloorTileRegistry INSTANCE = new FloorTileRegistry();
	
	private FloorTileRegistry() {
		super();
	}
	
	public static FloorTileRegistry instance() {
		return INSTANCE;
	}
	
	public FloorTile getTile(String key) {
		return get(key);
	}
	
	@Override
	public FloorTile getRegistered(String key) {
		return get(key);
	}

	@Override
	public void register(String key, FloorTile floorTile) {
		put(key, floorTile);
	}
	
}
