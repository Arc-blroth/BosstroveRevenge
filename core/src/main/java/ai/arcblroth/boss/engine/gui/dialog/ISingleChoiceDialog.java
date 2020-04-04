package ai.arcblroth.boss.engine.gui.dialog;

import java.util.function.Consumer;

public interface ISingleChoiceDialog extends IDialog {

	public void onChoice(Consumer<DialogOption> callback);

}
