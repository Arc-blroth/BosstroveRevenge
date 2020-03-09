package ai.arcblroth.boss.key;

import java.util.concurrent.ConcurrentHashMap;

public class KeyMap extends ConcurrentHashMap<Character, Keybind> {

	private static final KeyMap INSTANCE = new KeyMap();
	
	private KeyMap() {
		super();
	}
	
	public static KeyMap instance() {
		return INSTANCE;
	}
	
	public Keybind getRegistered(Character key) {
		return get(key);
	}

	public void register(Character key, Keybind keybind) {
		put(key, keybind);
	}
}
