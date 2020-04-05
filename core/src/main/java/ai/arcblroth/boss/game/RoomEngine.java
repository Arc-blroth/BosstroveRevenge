package ai.arcblroth.boss.game;

import ai.arcblroth.boss.engine.StepEvent;
import ai.arcblroth.boss.key.Keybind;

import java.util.List;

public abstract class RoomEngine {

	private WorldEngine worldEngine;

	public RoomEngine(WorldEngine worldEngine) {
		this.worldEngine = worldEngine;
	}

	public abstract void onRoomEnter();

	public abstract void step(StepEvent e);

	public abstract void handleKeybinds(List<Keybind> keybindList);

	public abstract void onRoomExit();

	public final WorldEngine getWorldEngine() {
		return worldEngine;
	}
}
