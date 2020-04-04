package ai.arcblroth.boss.engine.gui;

import ai.arcblroth.boss.engine.gui.dialog.MultipleChoiceGUIListDialog;
import ai.arcblroth.boss.engine.gui.dialog.SimpleDialogOption;
import ai.arcblroth.boss.engine.gui.dialog.SingleChoiceGUIListDialog;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GUIFactory {

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

	public static AnimatedGUITextPanel newAnimatedTextPanel(GUILookAndFeel lookAndFeel, String text) {
		return new AnimatedGUITextPanel(
				lookAndFeel.panelBgColor,
				lookAndFeel.panelBorderColor,
				lookAndFeel.textDeselectedBgColor,
				lookAndFeel.textDeselectedFgColor,
				lookAndFeel.panelBorderWidth,
				text,
				true,
				lookAndFeel.textAnimationSpeed
		);
	}

	public static AnimatedGUITextPanel newTextPanel(GUILookAndFeel lookAndFeel, String text) {
		return new AnimatedGUITextPanel(
				lookAndFeel.panelBgColor,
				lookAndFeel.panelBorderColor,
				lookAndFeel.textDeselectedBgColor,
				lookAndFeel.textDeselectedFgColor,
				lookAndFeel.panelBorderWidth,
				text,
				true,
				0
		);
	}

}
