package ai.arcblroth.boss.register;

import java.util.concurrent.ConcurrentHashMap;

import ai.arcblroth.boss.engine.Level;

public class LevelRegistry extends ConcurrentHashMap<String, Level> implements IRegistry<String, Level> {
	
	private static final LevelRegistry INSTANCE = new LevelRegistry();
	
	private LevelRegistry() {
		super();
	}
	
	public static LevelRegistry get() {
		return INSTANCE;
	}
	
	public Level getLevel(String key) {
		return get(key);
	}
	
	@Override
	public Level getRegistered(String key) {
		return get(key);
	}

	@Override
	public void register(String key, Level floorTile) {
		put(key, floorTile);
	}
	
}
