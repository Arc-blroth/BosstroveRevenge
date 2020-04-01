package ai.arcblroth.boss.engine.gui.dialog;

import ai.arcblroth.boss.render.Color;

public interface DialogOption {

	public String getOptionText();

	public void setSelected(boolean selected);

	public boolean isSelected();

	public Color getSelectedBackgroundColor();

	public Color getSelectedForegroundColor();

	public Color getDeselectedBackgroundColor();

	public Color getDeselectedForegroundColor();

}
