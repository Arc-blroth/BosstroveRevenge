package ai.arcblroth.boss.game.gui;

import ai.arcblroth.boss.engine.gui.*;
import ai.arcblroth.boss.game.WorldEngine;
import ai.arcblroth.boss.render.Color;

import java.util.Objects;

public class WorldGUI extends GUI {

	private WorldEngine worldEngine;
	private GUILookAndFeel lookAndFeel;
	private WorldDialoguePanel dialoguePanel;
	private HUD hud;
	private long toastTimer = 0;
	private GUIText toast;

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

		this.toast = new GUIText("[Toast]", lookAndFeel.textSelectedBgColor, lookAndFeel.textSelectedFgColor);
		add(toast, new CenteredTextGUIConstraints(toast, 1, 0.5, 0.8, 0, 0, 2));
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

	public void toast(long steps, String text) {
		toast.setText(text);
		toastTimer = steps;
	}

	public void advanceFrame() {
		if(dialoguePanel.isVisible()) dialoguePanel.advanceFrame();
		if(toastTimer > 0) {
			toast.setVisible(true);
		} else {
			toast.setVisible(false);
			toastTimer = 0;
		}
		toastTimer--;
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

	public GUIText getToastGUIText() {
		return toast;
	}
}