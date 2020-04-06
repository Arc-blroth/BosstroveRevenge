package ai.arcblroth.boss.game.room;

import ai.arcblroth.boss.engine.StepEvent;
import ai.arcblroth.boss.engine.gui.GUIConstraints;
import ai.arcblroth.boss.engine.gui.GUIFactory;
import ai.arcblroth.boss.engine.gui.WorldDialoguePanel;
import ai.arcblroth.boss.engine.gui.dialog.SingleChoiceGUIListDialog;
import ai.arcblroth.boss.game.RoomEngine;
import ai.arcblroth.boss.game.WorldEngine;
import ai.arcblroth.boss.game.cutscene.TopAndBottomBarShader;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.render.Color;

import java.util.List;

public class W0L1R0Engine extends RoomEngine {

	private WorldDialoguePanel funsies;

	public W0L1R0Engine(WorldEngine worldEngine) {
		super(worldEngine);
	}

	@Override
	public void onRoomEnter() {
		WorldEngine engine = getWorldEngine();
		engine.setGuiHasFocus(true);
		SingleChoiceGUIListDialog dialog = GUIFactory.newSingleChoiceListDialog(engine.getLookAndFeel(), "Yes", "No", "Maybe", "What?", "...", "Sure");
		funsies = new WorldDialoguePanel(engine.getLookAndFeel(), "Bosstrove", "Would you like to play a game?");
		dialog.onChoice(choice -> {
			engine.getGUI().remove(dialog);
			funsies.setTextString("Well, you're gonna have a bad time ;)");
			funsies.onAdvance(() -> {
				engine.getGUI().remove(funsies);
				engine.setGuiHasFocus(false);
			});
			engine.getGUI().setFocusedComponent(funsies);
		});
		engine.getGUI().add(funsies, new GUIConstraints("0", "0", "100%", "100%", 1));
		engine.getGUI().add(dialog, new GUIConstraints("5", "5", "12", "29", 3));
		engine.getGUI().setFocusedComponent(dialog);

		engine.setState(WorldEngine.State.IN_CUTSCENE);
		engine.getRenderer().putGlobalShader(0, new TopAndBottomBarShader(Color.BLACK, 0.05, 0.05));
	}

	@Override
	public void step(StepEvent e) {
		if(!funsies.isHidden()) {
			funsies.advanceFrame();
		}
	}

	@Override
	public void handleKeybinds(List<Keybind> keybindList) {

	}

	@Override
	public void onRoomExit() {

	}


}
