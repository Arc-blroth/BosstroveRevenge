package ai.arcblroth.boss.engine.gui.dialog;

import ai.arcblroth.boss.engine.gui.GUIConstraints;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.key.KeybindRegistry;
import ai.arcblroth.boss.render.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class SingleChoiceGUIListDialog extends AbstractGUIListDialog implements ISingleChoiceDialog {

	private ArrayList<Consumer<DialogOption>> callbacks = new ArrayList<>();

	public SingleChoiceGUIListDialog() {
		super();
	}

	public SingleChoiceGUIListDialog(List<DialogOption> options, Color backgroundColor, Color borderColor, int borderWidth) {
		super(options, backgroundColor, borderColor, borderWidth);
		if(options.get(getSelectedPosition()) != null) options.get(getSelectedPosition()).setSelected(true);
	}

	public SingleChoiceGUIListDialog(List<DialogOption> options, Color backgroundColor, Color borderColor, int borderWidth, GUIConstraints optionPadding) {
		super(options, backgroundColor, borderColor, borderWidth, optionPadding);
		if(options.get(getSelectedPosition()) != null) options.get(getSelectedPosition()).setSelected(true);
	}

	@Override
	public void onChoice(Consumer<DialogOption> callback) {
		callbacks.add(Objects.requireNonNull(callback));
	}

	@Override
	public void onInput(Keybind k) {
		DialogOption selectedOption = getOptions().get(getSelectedPosition());
		if (k.equals(KeybindRegistry.KEYBIND_UP)) {
			selectedOption.setSelected(false);
			setSelectedPosition(getSelectedPosition() - 1);
			getOptions().get(getSelectedPosition()).setSelected(true);
		}
		if (k.equals(KeybindRegistry.KEYBIND_DOWN)) {
			selectedOption.setSelected(false);
			setSelectedPosition(getSelectedPosition() + 1);
			getOptions().get(getSelectedPosition()).setSelected(true);
		}
		if (k.equals(KeybindRegistry.KEYBIND_USE) || k.equals(KeybindRegistry.KEYBIND_ENTER)) {
			callbacks.forEach(callback -> callback.accept(selectedOption));
		}
	}

}
