package ai.arcblroth.boss.engine.gui.dialog;

import ai.arcblroth.boss.engine.gui.GUILookAndFeel;

import java.util.Arrays;
import java.util.stream.Collectors;

public class DialogFactory {

	public static SingleChoiceGUIListDialog newSingleChoiceListDialog(GUILookAndFeel lookAndFeel, String... options) {
		return new SingleChoiceGUIListDialog(
				Arrays.stream(options)
						.map(text -> new SimpleDialogOption(text, lookAndFeel.textSelectedBgColor, lookAndFeel.textSelectedFgColor, lookAndFeel.textDeselectedBgColor, lookAndFeel.textDeselectedFgColor))
						.collect(Collectors.toList()),
				lookAndFeel.panelBgColor,
				lookAndFeel.panelBorderColor,
				lookAndFeel.panelBorderWidth
		);
	}

	public static MultipleChoiceGUIListDialog newMultipleChoiceListDialog(GUILookAndFeel lookAndFeel, String... options) {
		return new MultipleChoiceGUIListDialog(
				Arrays.stream(options)
						.map(text -> new SimpleDialogOption(text, lookAndFeel.textSelectedBgColor, lookAndFeel.textSelectedFgColor, lookAndFeel.textDeselectedBgColor, lookAndFeel.textDeselectedFgColor))
						.collect(Collectors.toList()),
				lookAndFeel.panelBgColor,
				lookAndFeel.panelBorderColor,
				lookAndFeel.panelBorderWidth
		);
	}

}
