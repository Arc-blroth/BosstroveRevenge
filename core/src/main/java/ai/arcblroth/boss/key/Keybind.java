package ai.arcblroth.boss.key;

import ai.arcblroth.boss.register.IRegistrable;
import ai.arcblroth.boss.util.StaticDefaults;

/**
 * Represents an abstracted key input.
 * Used with {@link ai.arcblroth.boss.key.KeybindRegistry KeybindRegistry}
 * to enable custom keybinds.
 */
public class Keybind implements IRegistrable<Keybind> {
	
	private final String keybindId;
	private final long firingDelay;

	public Keybind(String keybindId) {
		this(keybindId, StaticDefaults.DEFAULT_KEYBIND_DELAY);
	}

	public Keybind(String keybindId, long firingDelay) {
		this.keybindId = keybindId;
		this.firingDelay = firingDelay;
	}
	
	public String getKeybindId() {
		return keybindId;
	}

	public long getFiringDelay() {
		return firingDelay;
	}

	@Override
	public int hashCode() {
		return keybindId.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Keybind other = (Keybind) obj;
		if (keybindId == null) {
			if (other.keybindId != null)
				return false;
		} else if (!keybindId.equals(other.keybindId))
			return false;
		return true;
	}

}