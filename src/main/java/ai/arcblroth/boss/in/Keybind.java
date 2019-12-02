package ai.arcblroth.boss.in;

import ai.arcblroth.boss.register.IRegistrable;

/**
 * Represents an abstracted key input.
 * Supposed to be used with KeyMap to enable
 * custom keybinds.
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
