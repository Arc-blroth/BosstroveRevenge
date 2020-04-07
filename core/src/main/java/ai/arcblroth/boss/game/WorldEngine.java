package ai.arcblroth.boss.game;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.engine.IEngine;
import ai.arcblroth.boss.engine.IInteractable.Direction;
import ai.arcblroth.boss.engine.Level;
import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.StepEvent;
import ai.arcblroth.boss.engine.entity.player.Player;
import ai.arcblroth.boss.key.CharacterInputEvent;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.key.KeybindRegistry;
import ai.arcblroth.boss.register.LevelRegistry;
import ai.arcblroth.boss.util.Pair;
import ai.arcblroth.boss.util.StaticDefaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class WorldEngine implements IEngine {

	public enum State {
		IN_WORLD, IN_CUTSCENE, IN_BATTLE
	}

	private Logger logger;
	private State state;
	private WorldRenderer renderer;
	private Level level;
	private RoomEngine roomEngine;
	private WorldGUI gui;
	private boolean guiHasFocus;
	private String currentRoomId;
	private HashMap<Keybind, Long> firedKeys;
	private HashMap<Runnable, Long> maybeLater;

	public WorldEngine() {
		this.logger = Logger.getLogger("WorldEngine");
		this.state = State.IN_WORLD;
		this.level = LevelRegistry.instance().getLevel("w0l1", this);
		this.currentRoomId = level.getInitialRoom();
		this.roomEngine = getCurrentRoom().buildRoomEngine(this);
		this.gui = new WorldGUI(this);
		this.guiHasFocus = false;
		this.renderer = new WorldRenderer(level.getRoom(currentRoomId), gui);
		this.firedKeys = new HashMap<>();
		this.maybeLater = new HashMap<>();
		BosstrovesRevenge.instance().setResetColor(level.getRoom(currentRoomId).getResetColor());

		if(this.roomEngine != null) runLater(roomEngine::onRoomEnter, 1);
	}
	
	@Override
	public void step(StepEvent e) {
		Player player = level.getRoom(currentRoomId).getPlayer();
		
		firedKeys.replaceAll((key, stepsFiredAgo) -> Math.min(stepsFiredAgo + 1, key.getFiringDelay()));
		maybeLater.replaceAll((action, stepsToGo) -> stepsToGo - 1);
		Iterator<Map.Entry<Runnable, Long>> actuallyNow = maybeLater.entrySet().iterator();
		actuallyNow.forEachRemaining(entry -> {
			if(entry.getValue() == 0) {
				try {
					entry.getKey().run();
				} catch(Exception ahh) {
					logger.log(java.util.logging.Level.SEVERE, "Error while running future callback", ahh);
				}
			}
		});

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

		if(state != State.IN_BATTLE) {
			try {
				level.getRoom(currentRoomId).runStepCallbacks();
				level.getRoom(currentRoomId).runCollisionCallbacks(firingKeys);
			} catch(Exception ahh) {
				logger.log(java.util.logging.Level.SEVERE, "Error while running room callbacks", ahh);
			}

			if(guiHasFocus) {
				firingKeys.forEach(keybind -> {
					try {
						gui.onInput(keybind);
					} catch(Exception ahh) {
						logger.log(java.util.logging.Level.SEVERE, "Error while running GUI onInput callback", ahh);
					}
				});
			} else {
				if(state == State.IN_WORLD) {
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
			}
			if(roomEngine != null) {
				try {
					roomEngine.handleKeybinds(firingKeys);
				} catch(Exception ahh) {
					logger.log(java.util.logging.Level.SEVERE, "Error while running RoomEngine handleKeybinds function", ahh);
				}
				try {
					roomEngine.step(e);
				} catch(Exception ahh) {
					logger.log(java.util.logging.Level.SEVERE, "Error while running room step function", ahh);
				}
			}
		}

		if(firingKeys.contains(new Keybind("boss.debug"))) {
			BosstrovesRevenge.instance().setRendererShowingFPS(!BosstrovesRevenge.instance().isRendererShowingFPS());
		}
		
		firingKeys.clear();

		if(state == State.IN_WORLD) {
			Pair<Integer, Integer> outputSize = BosstrovesRevenge.instance().getOutputSize();
			renderer.setRenderOffset(
					player.getPosition().getX() * StaticDefaults.TILE_WIDTH - outputSize.getFirst() / 2D,
					player.getPosition().getY() * StaticDefaults.TILE_HEIGHT - outputSize.getSecond() / 2D
			);
		}

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

	/**
	 * Schedules a task to be run <tt>steps</tt> steps later.
	 * The callback will be executed at the very start of the step.
	 * @param task the task to run
	 * @param steps how many steps in the future this task will be run.
	 *              There are {@value ai.arcblroth.boss.util.StaticDefaults#STEPS_PER_SECOND} steps per second.
	 */
	public void runLater(Runnable task, long steps) {
		maybeLater.put(task, steps);
	}

	public WorldGUI getGUI() {
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
		if(roomEngine != null) runLater(roomEngine::onRoomEnter, 1);
	}

	public State getState() {
		return state;
	}

	/**
	 * Sets the world state <b>on start of the next game step</b>.
	 * @param state State to change to. Cannot be null.
	 */
	public void setState(State state) {
		if(state == null) throw new NullPointerException();
		runLater(() -> this.state = state, 1);
	}

}
