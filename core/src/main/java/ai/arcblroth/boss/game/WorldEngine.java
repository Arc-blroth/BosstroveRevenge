package ai.arcblroth.boss.game;

import java.util.*;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.engine.IEngine;
import ai.arcblroth.boss.engine.IInteractable.Direction;
import ai.arcblroth.boss.engine.Level;
import ai.arcblroth.boss.engine.StepEvent;
import ai.arcblroth.boss.engine.entity.player.Player;
import ai.arcblroth.boss.engine.gui.*;
import ai.arcblroth.boss.engine.gui.dialog.GUIListDialog;
import ai.arcblroth.boss.engine.gui.dialog.SimpleDialogOption;
import ai.arcblroth.boss.key.CharacterInputEvent;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.key.KeybindRegistry;
import ai.arcblroth.boss.register.LevelRegistry;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.util.Pair;
import ai.arcblroth.boss.util.StaticDefaults;

public class WorldEngine implements IEngine {

	private WorldRenderer renderer;
	private Level level;
	private GUI gui;
	private String currentRoom;
	private HashMap<Keybind, Long> firedKeys;

	public WorldEngine() {
		this.level = LevelRegistry.instance().getLevel("w0l1", this);
		currentRoom = level.getInitialRoom();
		this.gui = new GUI();
		this.renderer = new WorldRenderer(level.getRoom(currentRoom), gui);
		this.firedKeys = new HashMap<>();
		BosstrovesRevenge.instance().setResetColor(level.getRoom(currentRoom).getResetColor());

		GUIPanel panel = new GUIPanel(new Color(150, 0, 0, 255 * 2 / 3), Color.BLACK, 1);
		GUIPanel panel2 = new GUIPanel(new Color(0, 150, 250, 255 / 2), Color.BLACK, 1);
		gui.add(panel, new GUIConstraints(0, 0, 0.8, 0.8, 6, 6, -12, -12, 0));
		gui.add(panel2, new GUIConstraints(0.2, 0.2, 0.8, 0.8, 6, 6, -12, -12, 1));
		panel2.add(new GUIText("one fish two fish red fish blue fish reallylongwordthingybob", Color.TRANSPARENT, Color.WHITE), new GUIConstraints("2", "2", "100%", "100%", 1));
		//panel.add(new GUIImage(BosstrovesRevenge.instance().getTextureCache().get(new InternalResource("yeet.png"))), new GUIConstraints("2", "2", "100%", "100%", 1));

		Color selectedFgColor = Color.BLACK;
		Color selectedBgColor = Color.LIGHT_GRAY;
		Color deselectedFgColor = Color.WHITE;
		Color deselectedBgColor = Color.TRANSPARENT;

		panel.add(new GUIListDialog(
				Arrays.asList(
						new SimpleDialogOption("Yes", selectedBgColor, selectedFgColor, deselectedBgColor, deselectedFgColor),
						new SimpleDialogOption("No", selectedBgColor, selectedFgColor, deselectedBgColor, deselectedFgColor),
						new SimpleDialogOption("Maybe", selectedBgColor, selectedFgColor, deselectedBgColor, deselectedFgColor),
						new SimpleDialogOption("What?", selectedBgColor, selectedFgColor, deselectedBgColor, deselectedFgColor),
						new SimpleDialogOption("...", selectedBgColor, selectedFgColor, deselectedBgColor, deselectedFgColor),
						new SimpleDialogOption("Sure", selectedBgColor, selectedFgColor, deselectedBgColor, deselectedFgColor)
				),
				new Color(35, 103, 219, 255 * 2 / 3),
				Color.BLUE,
				1
		), new GUIConstraints("5", "5", "12", "29", 3));
	}
	
	@Override
	public void step(StepEvent e) {
		Player player = level.getRoom(currentRoom).getPlayer();

		level.getRoom(currentRoom).runStepCallbacks();
		
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
		
		level.getRoom(currentRoom).runCollisionCallbacks(firingKeys);
		
		if(firingKeys.contains(new Keybind("boss.debug"))) {
			BosstrovesRevenge.instance().setRendererShowingFPS(!BosstrovesRevenge.instance().isRendererShowingFPS());
		}
		if(firingKeys.contains(new Keybind("boss.up"))) {
			player.setDirection(Direction.NORTH);
			player.accelerate(Direction.NORTH, 0.25);
		}
		if(firingKeys.contains(new Keybind("boss.down"))) {
			player.setDirection(Direction.SOUTH);
			player.accelerate(Direction.SOUTH, 0.25);
		}
		if(firingKeys.contains(new Keybind("boss.left"))) {
			player.setDirection(Direction.WEST);
			player.accelerate(Direction.WEST, 0.25);
		}
		if(firingKeys.contains(new Keybind("boss.right"))) {
			player.setDirection(Direction.EAST);
			player.accelerate(Direction.EAST, 0.25);
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
		if(KeybindRegistry.instance().containsKey(e.getKey())) {
			Keybind keybind = KeybindRegistry.instance().getRegistered(e.getKey());
			if(!firedKeys.containsKey(keybind)) {
				firedKeys.put(keybind, -1L);
			} else {
				if(firedKeys.get(keybind) >= keybind.getFiringDelay()) {
					firedKeys.put(keybind, -1L);
				}
			}
		}
	}

	public GUI getGUI() {
		return gui;
	}

	@Override
	public WorldRenderer getRenderer() {
		return renderer;
	}

}
