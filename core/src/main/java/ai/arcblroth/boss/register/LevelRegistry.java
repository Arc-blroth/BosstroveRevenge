package ai.arcblroth.boss.register;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

import ai.arcblroth.boss.engine.Level;
import ai.arcblroth.boss.game.WorldEngine;

public class LevelRegistry {
	
	private static final LevelRegistry INSTANCE = new LevelRegistry();
	private final ConcurrentHashMap<String, Function<WorldEngine, Level>> map;
	
	private LevelRegistry() {
		map = new ConcurrentHashMap<>();
	}
	
	public static LevelRegistry instance() {
		return INSTANCE;
	}
	
	public Level getLevel(String key, WorldEngine engine) {
		return map.get(key).apply(engine);
	}
	
	public void register(String key, Function<WorldEngine, Level> level) {
		map.put(key, level);
	}
	
	public void forEach(BiConsumer<String, Function<WorldEngine, Level>> action) {
		map.forEach(action);
	}
	
	public boolean containsKey(String key) {
		return map.containsKey(key);
	}
	
	public boolean containsValue(Function<WorldEngine, Level> value) {
		return map.containsValue(value);
	}
	
	public boolean contains(Function<WorldEngine, Level> value) {
		return map.contains(value);
	}
	
	
}
