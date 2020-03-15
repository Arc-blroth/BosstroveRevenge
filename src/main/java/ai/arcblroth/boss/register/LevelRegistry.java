package ai.arcblroth.boss.register;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import ai.arcblroth.boss.engine.Level;

public class LevelRegistry {
	
	private static final LevelRegistry INSTANCE = new LevelRegistry();
	private final ConcurrentHashMap<String, Level> map;
	
	private LevelRegistry() {
		map = new ConcurrentHashMap<>();
	}
	
	public static LevelRegistry instance() {
		return INSTANCE;
	}
	
	public Level getLevel(String key) {
		return map.get(key);
	}
	
	public void register(String key, Level level) {
		map.put(key, level);
	}
	
	public void forEach(BiConsumer<String, Level> action) {
		map.forEach(action);
	}
	
	
}
