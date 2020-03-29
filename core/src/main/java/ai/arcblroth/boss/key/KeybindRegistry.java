package ai.arcblroth.boss.key;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class KeybindRegistry {

	private static final KeybindRegistry INSTANCE = new KeybindRegistry();
	private final ConcurrentHashMap<Character, Keybind> map;
	
	private KeybindRegistry() {
		map = new ConcurrentHashMap<>();
	}
	
	public static KeybindRegistry instance() {
		return INSTANCE;
	}
	
	public Keybind getRegistered(Character key) {
		return map.get(key);
	}

	public void register(Character key, Keybind keybind) {
		map.put(key, keybind);
	}
	
	public void forEach(BiConsumer<Character, Keybind> action) {
		map.forEach(action);
	}
	
	public boolean containsKey(Character key) {
		return map.containsKey(key);
	}
	
	public boolean containsValue(Keybind value) {
		return map.containsValue(value);
	}
	
	public boolean contains(Keybind value) {
		return map.contains(value);
	}
	
}
