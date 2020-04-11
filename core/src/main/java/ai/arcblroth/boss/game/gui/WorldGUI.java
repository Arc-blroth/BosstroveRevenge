package ai.arcblroth.boss.game.gui;

import ai.arcblroth.boss.engine.gui.GUI;
import ai.arcblroth.boss.engine.gui.GUIConstraints;
import ai.arcblroth.boss.engine.gui.GUILookAndFeel;
import ai.arcblroth.boss.engine.gui.WorldDialoguePanel;
import ai.arcblroth.boss.game.WorldEngine;
import ai.arcblroth.boss.render.Color;

import java.util.Objects;

public class WorldGUI extends GUI {

	private WorldEngine worldEngine;
	private GUILookAndFeel lookAndFeel;
	private WorldDialoguePanel dialoguePanel;
	private HUD hud;

	public WorldGUI(WorldEngine worldEngine) {
		super();
		this.worldEngine = worldEngine;

		this.lookAndFeel = new GUILookAndFeel();
		lookAndFeel.panelBgColor = new Color(35, 103, 219, 255 * 2 / 3);
		lookAndFeel.panelBorderColor = Color.BLUE;
		lookAndFeel.panelBorderWidth = 1;
		lookAndFeel.textSelectedFgColor = Color.BLACK;
		lookAndFeel.textSelectedBgColor = Color.LIGHT_GRAY;
		lookAndFeel.textDeselectedFgColor = Color.WHITE;
		lookAndFeel.textDeselectedBgColor = Color.TRANSPARENT;
		lookAndFeel.textAnimationSpeed = 1.4F;

		this.dialoguePanel = new WorldDialoguePanel(lookAndFeel, "[Name]", "[Text]");
		dialoguePanel.setVisible(false);
		add(dialoguePanel, new GUIConstraints("0", "0", "100%", "100%", 0));
		setFocusedComponent(dialoguePanel);

		this.hud = new HUD(worldEngine.getCurrentRoom().getPlayer(), lookAndFeel);
		add(hud, new GUIConstraints("0", "0", "100%", "100%", 1));
	}

	public void showQuickDialogue(String name, String text) {
		worldEngine.setGuiHasFocus(true);
		dialoguePanel.setNameString(name);
		dialoguePanel.setTextString(text);
		dialoguePanel.setVisible(true);
		dialoguePanel.onAdvance(() -> {
			dialoguePanel.setVisible(false);
			worldEngine.setGuiHasFocus(false);
		});
	}

	public WorldEngine getWorldEngine() {
		return worldEngine;
	}

	public GUILookAndFeel getLookAndFeel() {
		return lookAndFeel;
	}

	public void setLookAndFeel(GUILookAndFeel lookAndFeel) {
		this.lookAndFeel = Objects.requireNonNull(lookAndFeel);
	}

	public WorldDialoguePanel getDialoguePanel() {
		return dialoguePanel;
	}

}