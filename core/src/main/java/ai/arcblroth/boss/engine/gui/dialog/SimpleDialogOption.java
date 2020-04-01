package ai.arcblroth.boss.engine.gui.dialog;

import ai.arcblroth.boss.render.Color;

public class SimpleDialogOption implements DialogOption {

	private String optionText;
	private boolean selected;
	private Color selectedBackgroundColor;
	private Color selectedForegroundColor;
	private Color deselectedBackgroundColor;
	private Color deselectedForegroundColor;

	public SimpleDialogOption(String optionText, Color selectedBackgroundColor, Color selectedForegroundColor, Color deselectedBackgroundColor, Color deselectedForegroundColor) {
		this.optionText = optionText;
		this.selectedBackgroundColor = selectedBackgroundColor;
		this.selectedForegroundColor = selectedForegroundColor;
		this.deselectedBackgroundColor = deselectedBackgroundColor;
		this.deselectedForegroundColor = deselectedForegroundColor;
	}

	@Override
	public String getOptionText() {
		return optionText;
	}

	@Override
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public Color getSelectedBackgroundColor() {
		return selectedBackgroundColor;
	}

	@Override
	public Color getSelectedForegroundColor() {
		return selectedForegroundColor;
	}

	@Override
	public Color getDeselectedBackgroundColor() {
		return deselectedBackgroundColor;
	}

	@Override
	public Color getDeselectedForegroundColor() {
		return deselectedForegroundColor;
	}

}
