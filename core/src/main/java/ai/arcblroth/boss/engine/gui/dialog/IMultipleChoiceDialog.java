package ai.arcblroth.boss.engine.gui.dialog;

import java.util.List;
import java.util.function.Consumer;

public interface IMultipleChoiceDialog extends IDialog {
	
	public void onChoice(Consumer<List<DialogOption>> callback);
	
}
