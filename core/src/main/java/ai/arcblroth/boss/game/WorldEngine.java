package ai.arcblroth.boss.game;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.engine.IEngine;
import ai.arcblroth.boss.engine.IInteractable.Direction;
import ai.arcblroth.boss.engine.Level;
import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.StepEvent;
import ai.arcblroth.boss.engine.entity.player.Player;
import ai.arcblroth.boss.engine.gui.GUI;
import ai.arcblroth.boss.engine.gui.GUILookAndFeel;
import ai.arcblroth.boss.key.CharacterInputEvent;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.key.KeybindRegistry;
import ai.arcblroth.boss.register.LevelRegistry;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.util.Pair;
import ai.arcblroth.boss.util.StaticDefaults;

import java.util.*;

public class WorldEngine implements IEngine {

	private WorldRenderer renderer;
	private Level level;
	private RoomEngine roomEngine;
	private GUI gui;
	private GUILookAndFeel lookAndFeel;
	private boolean guiHasFocus;
	private String currentRoomId;
	private HashMap<Keybind, Long> firedKeys;

	public WorldEngine() {
		this.level = LevelRegistry.instance().getLevel("w0l1", this);
		this.currentRoomId = level.getInitialRoom();
		this.roomEngine = getCurrentRoom().buildRoomEngine(this);
		this.gui = new GUI();
		this.guiHasFocus = false;
		this.renderer = new WorldRenderer(level.getRoom(currentRoomId), gui);
		this.firedKeys = new HashMap<>();
		BosstrovesRevenge.instance().setResetColor(level.getRoom(currentRoomId).getResetColor());

		lookAndFeel = new GUILookAndFeel();
		lookAndFeel.panelBgColor = new Color(35, 103, 219, 255 * 2 / 3);
		lookAndFeel.panelBorderColor = Color.BLUE;
		lookAndFeel.panelBorderWidth = 1;
		lookAndFeel.textSelectedFgColor = Color.BLACK;
		lookAndFeel.textSelectedBgColor = Color.LIGHT_GRAY;
		lookAndFeel.textDeselectedFgColor = Color.WHITE;
		lookAndFeel.textDeselectedBgColor = Color.TRANSPARENT;
		lookAndFeel.textAnimationSpeed = 1.4F;

		if(this.roomEngine != null) this.roomEngine.onRoomEnter();
	}
	
	@Override
	public void step(StepEvent e) {
		Player player = level.getRoom(currentRoomId).getPlayer();

		level.getRoom(currentRoomId).runStepCallbacks();
		
		firedKeys.replaceAll((key, stepsFiredAgo) -> Math.min(stepsFiredAgo + 1, key.getFiringDelay()));
		ArrayList<Keybind> firingKeys = new ArrayList<>();
		Iterator<Map.Entry<Keybind, Long>> firedKeysIterator = firedKeys.entrySet().iterator();
		firedKeysIterator.forEachRemaining(entry -> {
			if(entry.getValue() == 0) {
				firingKeys.add(entry.getKey());
				if(entry.getKey().getFiringDelay() == 0) {
					firedKeysIterator.remove();
				}
			}
		});
		
		level.getRoom(currentRoomId).runCollisionCallbacks(firingKeys);
		
		if(firingKeys.contains(new Keybind("boss.debug"))) {
			BosstrovesRevenge.instance().setRendererShowingFPS(!BosstrovesRevenge.instance().isRendererShowingFPS());
		}

		if(guiHasFocus) {
			firingKeys.forEach(gui::onInput);
		} else {
			if (firingKeys.contains(new Keybind("boss.north"))) {
				player.setDirection(Direction.NORTH);
				player.accelerate(Direction.NORTH, 0.25);
			}
			if (firingKeys.contains(new Keybind("boss.south"))) {
				player.setDirection(Direction.SOUTH);
				player.accelerate(Direction.SOUTH, 0.25);
			}
			if (firingKeys.contains(new Keybind("boss.west"))) {
				player.setDirection(Direction.WEST);
				player.accelerate(Direction.WEST, 0.25);
			}
			if (firingKeys.contains(new Keybind("boss.east"))) {
				player.setDirection(Direction.EAST);
				player.accelerate(Direction.EAST, 0.25);
			}
		}
		if(roomEngine != null) {
			roomEngine.handleKeybinds(firingKeys);
			roomEngine.step(e);
		}
		
		firingKeys.clear();

		Pair<Integer, Integer> outputSize = BosstrovesRevenge.instance().getOutputSize();
		renderer.setRenderOffset(
				player.getPosition().getX() * StaticDefaults.TILE_WIDTH - outputSize.getFirst() / 2D,
				player.getPosition().getY() * StaticDefaults.TILE_HEIGHT - outputSize.getSecond() / 2D
		);

	}

	@Override
	public void handleKeyInput(CharacterInputEvent e) {
		if(KeybindRegistry.instance().containsValue(e.getKey())) {
			KeybindRegistry.instance().getRegistered(e.getKey()).forEach(keybind -> {
				if (!firedKeys.containsKey(keybind)) {
					firedKeys.put(keybind, -1L);
				} else {
					if (firedKeys.get(keybind) >= keybind.getFiringDelay()) {
						firedKeys.put(keybind, -1L);
					}
				}
			});
		}
	}

	public GUI getGUI() {
		return gui;
	}

	public boolean doesGuiHasFocus() {
		return guiHasFocus;
	}

	public void setGuiHasFocus(boolean guiHasFocus) {
		this.guiHasFocus = guiHasFocus;
	}

	@Override
	public WorldRenderer getRenderer() {
		return renderer;
	}

	public Level getLevel() {
		return level;
	}

	public Room getCurrentRoom() {
		return level.getRoom(currentRoomId);
	}

	public String getCurrentRoomId() {
		return currentRoomId;
	}

	public void setCurrentRoomId(String roomId) {
		if(!level.hasRoom(roomId)) throw new IllegalArgumentException("Tried to set current room to a room not in the level!");
		currentRoomId = roomId;
		if(roomEngine != null) roomEngine.onRoomExit();
		roomEngine = getCurrentRoom().buildRoomEngine(this);
		renderer.setRoom(getCurrentRoom());
		if(roomEngine != null) roomEngine.onRoomEnter();
	}

	public GUILookAndFeel getLookAndFeel() {
		return lookAndFeel;
	}

	public void setLookAndFeel(GUILookAndFeel lookAndFeel) {
		this.lookAndFeel = Objects.requireNonNull(lookAndFeel);
	}

}
