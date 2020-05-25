package ai.arcblroth.boss.engine.gui.dialog;

import ai.arcblroth.boss.engine.gui.GUIConstraints;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.key.KeybindRegistry;
import ai.arcblroth.boss.render.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MultipleChoiceGUIListDialog extends AbstractGUIListDialog implements IMultipleChoiceDialog {

	private ArrayList<Consumer<List<DialogOption>>> callbacks = new ArrayList<>();

	public MultipleChoiceGUIListDialog() {
		super();
	}

	public MultipleChoiceGUIListDialog(List<DialogOption> options, Color backgroundColor, Color borderColor, int borderWidth) {
		super(options, backgroundColor, borderColor, borderWidth);
	}

	public MultipleChoiceGUIListDialog(List<DialogOption> options, Color backgroundColor, Color borderColor, int borderWidth, GUIConstraints optionPadding) {
		super(options, backgroundColor, borderColor, borderWidth, optionPadding);
	}

	@Override
	public void onChoice(Consumer<List<DialogOption>> callback) {
		callbacks.add(Objects.requireNonNull(callback));
	}

	@Override
	public void onInput(Keybind k) {
		if (k.equals(KeybindRegistry.KEYBIND_UP)) {
			setSelectedPosition(getSelectedPosition() - 1);
		}
		if (k.equals(KeybindRegistry.KEYBIND_DOWN)) {
			setSelectedPosition(getSelectedPosition() + 1);
		}
		if (k.equals(KeybindRegistry.KEYBIND_USE)) {
			DialogOption option = getOptions().get(getSelectedPosition());
			option.setSelected(!option.isSelected());
		}
		if (k.equals(KeybindRegistry.KEYBIND_ENTER)) {
			List<DialogOption> selectedOptions = getOptions().stream().filter(DialogOption::isSelected).collect(Collectors.toList());
			if(selectedOptions.size() > 0) {
				callbacks.forEach(callback -> callback.accept(selectedOptions));
			}
		}
	}

}
