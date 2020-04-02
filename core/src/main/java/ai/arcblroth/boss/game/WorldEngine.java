package ai.arcblroth.boss.game;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.engine.IEngine;
import ai.arcblroth.boss.engine.IInteractable.Direction;
import ai.arcblroth.boss.engine.Level;
import ai.arcblroth.boss.engine.StepEvent;
import ai.arcblroth.boss.engine.entity.player.Player;
import ai.arcblroth.boss.engine.gui.*;
import ai.arcblroth.boss.engine.gui.dialog.DialogFactory;
import ai.arcblroth.boss.engine.gui.dialog.SingleChoiceGUIListDialog;
import ai.arcblroth.boss.key.CharacterInputEvent;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.key.KeybindRegistry;
import ai.arcblroth.boss.register.LevelRegistry;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.util.Pair;
import ai.arcblroth.boss.util.StaticDefaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WorldEngine implements IEngine {

	private WorldRenderer renderer;
	private Level level;
	private GUI gui;
	private GUILookAndFeel lookAndFeel;
	private boolean guiHasFocus;
	private String currentRoom;
	private HashMap<Keybind, Long> firedKeys;

	public WorldEngine() {
		this.level = LevelRegistry.instance().getLevel("w0l1", this);
		currentRoom = level.getInitialRoom();
		this.gui = new GUI();
		this.guiHasFocus = false;
		this.renderer = new WorldRenderer(level.getRoom(currentRoom), gui);
		this.firedKeys = new HashMap<>();
		BosstrovesRevenge.instance().setResetColor(level.getRoom(currentRoom).getResetColor());

		setGuiHasFocus(true);

		lookAndFeel = new GUILookAndFeel();
		lookAndFeel.panelBgColor = new Color(35, 103, 219, 255 * 2 / 3);
		lookAndFeel.panelBorderColor = Color.BLUE;
		lookAndFeel.panelBorderWidth = 1;
		lookAndFeel.textSelectedFgColor = Color.BLACK;
		lookAndFeel.textSelectedBgColor = Color.LIGHT_GRAY;
		lookAndFeel.textDeselectedFgColor = Color.WHITE;
		lookAndFeel.textDeselectedBgColor = Color.TRANSPARENT;

		GUIPanel panel = new GUIPanel(new Color(150, 0, 0, 255 * 2 / 3), Color.BLACK, 1);
		GUIPanel panel2 = new GUIPanel(new Color(0, 150, 250, 255 / 2), Color.BLACK, 1);
		gui.add(panel, new GUIConstraints(0, 0, 0.8, 0.8, 6, 6, -12, -12, 0));
		gui.add(panel2, new GUIConstraints(0.2, 0.2, 0.8, 0.8, 6, 6, -12, -12, 1));
		panel2.add(new GUIText("one fish two fish red fish blue fish reallylongwordthingybob", Color.TRANSPARENT, Color.WHITE), new GUIConstraints("2", "2", "100%", "100%", 1));
		//panel.add(new GUIImage(BosstrovesRevenge.instance().getTextureCache().get(new InternalResource("yeet.png"))), new GUIConstraints("2", "2", "100%", "100%", 1));

		SingleChoiceGUIListDialog dialog = DialogFactory.newSingleChoiceListDialog(lookAndFeel, "Yes", "No", "Maybe", "What?", "...", "Sure");
		dialog.onChoice(choice -> {
			panel.remove(dialog);
			setGuiHasFocus(false);
		});
		panel.add(dialog, new GUIConstraints("5", "5", "12", "29", 3));

		gui.setFocusedComponentRecursively(dialog);
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

}
