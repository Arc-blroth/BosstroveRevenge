package ai.arcblroth.boss.key;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class KeybindRegistry {

	private static final KeybindRegistry INSTANCE = new KeybindRegistry();
	private final ConcurrentHashMap<Keybind, Character> map;
	
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
