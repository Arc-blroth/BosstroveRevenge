package ai.arcblroth.boss.engine.gui;

import ai.arcblroth.boss.engine.gui.dialog.IAdvanceableDialog;

import java.util.ArrayList;

public class WorldDialoguePanel extends GUIParent implements IAdvanceableDialog {

	private ArrayList<Runnable> callbacks = new ArrayList<>();
	private GUILookAndFeel lookAndFeel;
	private AnimatedGUITextPanel namePanel;
	private AnimatedGUITextPanel textPanel;
	private String nameString;
	private String textString;

	public WorldDialoguePanel(GUILookAndFeel lookAndFeel) {
		this(lookAndFeel, "", "");
	}

	public WorldDialoguePanel(GUILookAndFeel lookAndFeel, String nameString, String textString) {
		this.lookAndFeel = lookAndFeel;
		this.nameString = nameString;
		this.textString = textString;
		this.namePanel = GUIFactory.newTextPanel(lookAndFeel, nameString);
		this.textPanel = GUIFactory.newAnimatedTextPanel(lookAndFeel, textString);
		this.textPanel.onAdvance(() -> {
			callbacks.forEach(Runnable::run);
			callbacks.clear();
		});
		update();
	}

	public void advanceFrame() {
		if(textPanel.canAdvanceFrame()) textPanel.advanceFrame();
	}

	/**
	 * Registers a <b>one time</b> advance callback for the dialogue panel.
	 * @param callback called when the user presses the boss.use or boss.enter keybind after this panel has finished animating.
	 */
	@Override
	public void onAdvance(Runnable callback) {
		callbacks.add(callback);
	}

	public GUILookAndFeel getLookAndFeel() {
		return lookAndFeel;
	}

	public synchronized void setLookAndFeel(GUILookAndFeel lookAndFeel) {
		this.lookAndFeel = lookAndFeel;
		updateLookAndFeel();
	}

	public synchronized void updateLookAndFeel() {
		update();
	}

	public String getNameString() {
		return nameString;
	}

	public synchronized void setNameString(String nameString) {
		this.nameString = nameString;
		update();
	}

	public String getTextString() {
		return textString;
	}

	public synchronized void setTextString(String textString) {
		this.textString = textString;
		update();
	}

	private synchronized void update() {
		this.remove(namePanel);
		this.remove(textPanel);
		this.namePanel.setText(nameString);
		this.textPanel.setText(textString);
		this.add(namePanel, new NamePanelGUIConstrants(nameString.length()));
		this.add(textPanel, new TextPanelGUIConstraints());
		setFocusedComponent(textPanel);
	}

	private class NamePanelGUIConstrants extends GUIConstraints {

		public NamePanelGUIConstrants(int nameLength) {
			super(0, 0.75, 0, 0, 2, -4, nameLength == 0 ? 0 : nameLength + 4, 5, 1);
		}

		@Override
		public int resolveY(int guiWidth, int guiHeight) {
			return super.resolveY(guiWidth, guiHeight) - ((int)Math.round(0.75 * guiHeight) % 2 == 0 ? 1 : 0);
		}

	}

	private class TextPanelGUIConstraints extends GUIConstraints {

		public TextPanelGUIConstraints() {
			super(0, 0.75, 1, 0.25, 2, 0, -4, -2, 0);
		}

		@Override
		public int resolveY(int guiWidth, int guiHeight) {
			return super.resolveY(guiWidth, guiHeight) - ((int)Math.round(0.75 * guiHeight) % 2 == 0 ? 1 : 0);
		}

	}

}
