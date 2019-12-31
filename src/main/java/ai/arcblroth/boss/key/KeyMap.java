package ai.arcblroth.boss.key;

import java.util.concurrent.ConcurrentHashMap;

import ai.arcblroth.boss.register.IRegistry;

public class KeyMap extends ConcurrentHashMap<Character, Keybind> implements IRegistry<Character, Keybind> {

	private static final KeyMap INSTANCE = new KeyMap();
	
	private KeyMap() {
		super();
	}
	
	public static KeyMap get() {
		return INSTANCE;
	}
	
	public Keybind getTile(Character key) {
		return get(key);
	}
	
	public Keybind getRegistered(Character key) {
		return get(key);
	}

	public void register(Character key, Keybind floorTile) {
		put(key, floorTile);
	}
}
