package ai.arcblroth.boss.engine.gui.dialog;

import java.util.function.Consumer;

public interface ISingleChoiceDialog {

	public void onChoice(Consumer<DialogOption> callback);

}
