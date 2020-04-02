package ai.arcblroth.boss.engine.gui.dialog;

import ai.arcblroth.boss.engine.gui.GUILookAndFeel;

import java.util.Arrays;
import java.util.stream.Collectors;

public class DialogFactory {

	public static GUIListDialog newSimpleListDialog(GUILookAndFeel lookAndFeel, String... options) {
		return new GUIListDialog(
				Arrays.stream(options)
						.map(text -> new SimpleDialogOption(text, lookAndFeel.textSelectedBgColor, lookAndFeel.textSelectedFgColor, lookAndFeel.textDeselectedBgColor, lookAndFeel.textDeselectedFgColor))
						.collect(Collectors.toList()),
				lookAndFeel.panelBgColor,
				lookAndFeel.panelBorderColor,
				lookAndFeel.panelBorderWidth
		);
	}

}
