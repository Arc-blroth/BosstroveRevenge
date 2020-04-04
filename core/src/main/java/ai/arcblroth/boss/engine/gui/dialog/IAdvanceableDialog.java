package ai.arcblroth.boss.engine.gui.dialog;

public interface IAdvanceableDialog extends IDialog {

	public void onAdvance(Runnable callback);

}