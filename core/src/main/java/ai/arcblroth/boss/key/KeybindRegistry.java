package ai.arcblroth.boss.key;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class KeybindRegistry {

	private static final KeybindRegistry INSTANCE = new KeybindRegistry();
	private final ConcurrentHashMap<Keybind, Character> map;

	public static final Keybind KEYBIND_USE = new Keybind("boss.use");
	public static final Keybind KEYBIND_ENTER = new Keybind("boss.enter");
	public static final Keybind KEYBIND_TOGGLE_DEBUG = new Keybind("boss.debug");
	public static final Keybind KEYBIND_MOVE_NORTH = new Keybind("boss.north", 0);
	public static final Keybind KEYBIND_MOVE_SOUTH = new Keybind("boss.south", 0);
	public static final Keybind KEYBIND_MOVE_WEST = new Keybind("boss.west", 0);
	public static final Keybind KEYBIND_MOVE_EAST = new Keybind("boss.east", 0);
	public static final Keybind KEYBIND_UP = new Keybind("boss.up");
	public static final Keybind KEYBIND_DOWN = new Keybind("boss.down");
	public static final Keybind KEYBIND_LEFT = new Keybind("boss.left");
	public static final Keybind KEYBIND_RIGHT = new Keybind("boss.right");

	static {
		INSTANCE.register(KEYBIND_USE, ' ');
		INSTANCE.register(KEYBIND_ENTER, '\n');
		INSTANCE.register(KEYBIND_TOGGLE_DEBUG, '`');
		INSTANCE.register(KEYBIND_MOVE_NORTH, 'w');
		INSTANCE.register(KEYBIND_MOVE_WEST, 'a');
		INSTANCE.register(KEYBIND_MOVE_SOUTH, 's');
		INSTANCE.register(KEYBIND_MOVE_EAST, 'd');
		INSTANCE.register(KEYBIND_UP, 'w');
		INSTANCE.register(KEYBIND_LEFT, 'a');
		INSTANCE.register(KEYBIND_DOWN, 's');
		INSTANCE.register(KEYBIND_RIGHT, 'd');
	}

	private KeybindRegistry() {
		map = new ConcurrentHashMap<>();
	}
	
	public static KeybindRegistry instance() {
		return INSTANCE;
	}
	
	public Character getRegistered(Keybind key) {
		return map.get(key);
	}

	public List<Keybind> getRegistered(Character key) {
		ArrayList<Keybind> list = new ArrayList<>();
		map.forEach((keybind, character) -> {
			if(key.equals(character)) list.add(keybind);
		});
		return list;
	}

	public void register(Keybind keybind, Character key) {
		map.put(keybind, key);
	}
	
	public void forEach(BiConsumer<Keybind, Character> action) {
		map.forEach(action);
	}

	public boolean containsKey(Keybind key) {
		return map.containsKey(key);
	}
	
	public boolean containsValue(Character value) {
		return map.containsValue(value);
	}
	
	public boolean contains(Character value) {
		return map.contains(value);
	}
	
}
