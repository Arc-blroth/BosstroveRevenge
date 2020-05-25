package ai.arcblroth.boss.game.area;

import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.area.Area;
import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.engine.entity.player.Player;
import ai.arcblroth.boss.engine.hitbox.Hitbox;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.key.KeybindRegistry;
import com.google.gson.JsonObject;

public class FlavorTextArea extends Area {

	private final String flavorName;
	private final String flavorText;

	public FlavorTextArea(Room room, Hitbox hitbox, JsonObject context) {
		super(room, hitbox, context);
		this.flavorName = context.has("name") ? context.get("name").getAsString() : "Flavor Text";
		this.flavorText = context.has("text") ? context.get("text").getAsString() : "You see nothing!\n(This shouldn't be here, please report!)";
	}

	@Override
	public void onEntityStep(IEntity e) {
		if(e instanceof Player) {
			if(!getRoom().getLevel().getEngine().doesGuiHasFocus()) {
				getRoom().getLevel().getEngine().getGUI().toast(1, "Press SPACE or ENTER to show flavor text");
			}
		}
	}

	@Override
	public void onPlayerInteract(Keybind keybind) {
		if(keybind.equals(KeybindRegistry.KEYBIND_USE) || keybind.equals(KeybindRegistry.KEYBIND_ENTER)) {
			if(!getRoom().getLevel().getEngine().doesGuiHasFocus()) {
				getRoom().getLevel().getEngine().getGUI().showQuickDialogue(flavorName, flavorText);
			}
		}
	}

	@Override
	public String getId() {
		return "boss.flavorText";
	}

}
