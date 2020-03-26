package ai.arcblroth.boss.game;

import java.util.ArrayList;
import java.util.HashMap;
import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.engine.IEngine;
import ai.arcblroth.boss.engine.IInteractable.Direction;
import ai.arcblroth.boss.engine.Level;
import ai.arcblroth.boss.engine.StepEvent;
import ai.arcblroth.boss.engine.entity.player.Player;
import ai.arcblroth.boss.engine.gui.GUI;
import ai.arcblroth.boss.engine.gui.GUIConstraints;
import ai.arcblroth.boss.engine.gui.GUIPanel;
import ai.arcblroth.boss.engine.gui.GUIText;
import ai.arcblroth.boss.key.CharacterInputEvent;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.key.KeybindRegistry;
import ai.arcblroth.boss.register.LevelRegistry;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.IRenderer;
import ai.arcblroth.boss.util.Pair;
import ai.arcblroth.boss.util.StaticDefaults;

public class WorldEngine implements IEngine {

	private WorldRenderer renderer;
	private Level level;
	private String currentRoom;
	private HashMap<Keybind, Long> firedKeys;

	public WorldEngine() {
		this.level = LevelRegistry.instance().getLevel("w0l1");
		currentRoom = "0";
		this.renderer = new WorldRenderer(level.getRoom(currentRoom), level.getGui());
		this.firedKeys = new HashMap<>();
		BosstrovesRevenge.instance().setResetColor(level.getRoom(currentRoom).getResetColor());

		Color gradient = new Color(0, 0, 0, 255 * 2 / 3);
		level.getGui().add(new GUIPanel(gradient, gradient), new GUIConstraints(0, 0, 1, 1, 5, 5, -10, -10, 0));
		level.getGui().add(new GUIText("one fish two fish red fish blue fish reallylongwordthingybob"), new GUIConstraints("2px", "2px", "100%", "100%", 1));
	}
	
	@Override
	public void step(StepEvent e) {
		
		level.getRoom(currentRoom).runStepCallbacks();
		
		firedKeys.replaceAll((key, stepsFiredAgo) -> Math.min(stepsFiredAgo + 1, StaticDefaults.KEYBIND_DELAY));
		ArrayList<Keybind> firingKeys = new ArrayList<>();
		firedKeys.forEach((key, stepsFiredAgo) -> {
			if(stepsFiredAgo == 0) {
				firingKeys.add(key);
			}
		});
		
		level.getRoom(currentRoom).runCollisionCallbacks(firingKeys);
		
		if(firingKeys.contains(new Keybind("boss.debug"))) {
			BosstrovesRevenge.instance().setRendererShowingFPS(!BosstrovesRevenge.instance().isRendererShowingFPS());
		}
		
		firingKeys.clear();
		
		Player player = level.getRoom(currentRoom).getPlayer();
		Pair<Integer, Integer> outputSize = BosstrovesRevenge.instance().getOutputSize();
		renderer.setRenderOffset(
				player.getPosition().getX() * StaticDefaults.TILE_WIDTH - outputSize.getFirst() / 2D,
				player.getPosition().getY() * StaticDefaults.TILE_HEIGHT - outputSize.getSecond() / 2D
		);
	}

	@Override
	public void handleKeyInput(CharacterInputEvent e) {
		Player player = level.getRoom(currentRoom).getPlayer();
		if(e.getKey() == 'w') {
			player.setDirection(Direction.NORTH);
			player.accelerate(Direction.NORTH, 0.25);
		} else if(e.getKey() == 'd') {
			player.setDirection(Direction.EAST);
			player.accelerate(Direction.EAST, 0.25);
		} else if(e.getKey() == 'a') {
			player.setDirection(Direction.WEST);
			player.accelerate(Direction.WEST, 0.25);
		} else if(e.getKey() == 's') {
			player.setDirection(Direction.SOUTH);
			player.accelerate(Direction.SOUTH, 0.25);
		}
		
		if(KeybindRegistry.instance().containsKey(e.getKey())) {
			Keybind keybind = KeybindRegistry.instance().getRegistered(e.getKey());
			if(!firedKeys.containsKey(keybind)) {
				firedKeys.put(keybind, -1L);
			} else {
				if(firedKeys.get(keybind) >= StaticDefaults.KEYBIND_DELAY) {
					firedKeys.put(keybind, -1L);
				}
			}
		}
	}

	@Override
	public IRenderer getRenderer() {
		return renderer;
	}

}
