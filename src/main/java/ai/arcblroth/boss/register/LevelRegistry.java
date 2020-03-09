package ai.arcblroth.boss.register;

import java.util.concurrent.ConcurrentHashMap;

import ai.arcblroth.boss.engine.Level;

public class LevelRegistry extends ConcurrentHashMap<String, Level> {
	
	private static final LevelRegistry INSTANCE = new LevelRegistry();
	
	private LevelRegistry() {
		super();
	}
	
	public static LevelRegistry instance() {
		return INSTANCE;
	}
	
	public Level getLevel(String key) {
		return get(key);
	}
	
	public void register(String key, Level level) {
		put(key, level);
	}
	
}
