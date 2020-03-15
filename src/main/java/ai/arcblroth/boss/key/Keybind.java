package ai.arcblroth.boss.key;

import ai.arcblroth.boss.register.IRegistrable;

/**
 * Represents an abstracted key input.
 * Used with {@link ai.arcblroth.boss.key.KeybindRegistry KeybindRegistry}
 * to enable custom keybinds.
 */
public class Keybind implements IRegistrable<Keybind> {
	
	private String keybindName;

	public Keybind(String keybindName) {
		this.keybindName = keybindName;
	}
	
	public String getKeybindName() {
		return keybindName;
	}
	
}
