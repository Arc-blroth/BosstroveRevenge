package ai.arcblroth.boss.game.room;

import ai.arcblroth.boss.engine.StepEvent;
import ai.arcblroth.boss.engine.gui.GUIConstraints;
import ai.arcblroth.boss.engine.gui.GUIFactory;
import ai.arcblroth.boss.engine.gui.dialog.SingleChoiceGUIListDialog;
import ai.arcblroth.boss.game.RoomEngine;
import ai.arcblroth.boss.game.WorldEngine;
import ai.arcblroth.boss.key.Keybind;

import java.util.List;

public class W0L1R0Engine extends RoomEngine {

	public W0L1R0Engine(WorldEngine worldEngine) {
		super(worldEngine);
	}

	@Override
	public void onRoomEnter() {
		worldEngine.setGuiHasFocus(true);
		SingleChoiceGUIListDialog dialog = GUIFactory.newSingleChoiceListDialog(worldEngine.getGUI().getLookAndFeel(), "Yes", "No", "Maybe", "What?", "...", "Sure");
		dialog.onChoice(choice -> {
			worldEngine.getGUI().remove(dialog);
			worldEngine.getGUI().getDialoguePanel().setTextString("Well, you're gonna have a bad time ;)");
			worldEngine.getGUI().getDialoguePanel().onAdvance(() -> {
				worldEngine.getGUI().getDialoguePanel().setVisible(false);
				worldEngine.setGuiHasFocus(false);
			});
			worldEngine.getGUI().setFocusedComponent(worldEngine.getGUI().getDialoguePanel());
		});
		dialog.setVisible(true);
		worldEngine.getGUI().getDialoguePanel().setNameString("Bosstrove");
		worldEngine.getGUI().getDialoguePanel().setTextString("Would you like to play a game?");
		worldEngine.getGUI().add(dialog, new GUIConstraints("5", "5", "12", "29", 1));
		worldEngine.getGUI().setFocusedComponent(dialog);

		//worldEngine.getRenderer().putGlobalShader(0, new TopAndBottomBarShader(Color.BLACK, 0.05, 0.05));
	}

	@Override
	public void step(StepEvent e) {
		if(worldEngine.getGUI().getDialoguePanel().isVisible()) {
			worldEngine.getGUI().getDialoguePanel().advanceFrame();
		}
	}

	@Override
	public void handleKeybinds(List<Keybind> keybindList) {

	}

	@Override
	public void onRoomExit() {

	}


}
