package ai.arcblroth.boss.engine.gui;

public class CenteredTextGUIConstraints extends GUIConstraints {

	private GUIText text;
	private int xOffset;
	private int yOffset;

	public CenteredTextGUIConstraints(GUIText text, int linesOfText, int xOffset, int yOffset, int zOrder) {
		super(0.5, 0.5, 1, 1, 0, -2 * linesOfText / 2, 0, 0, zOrder);
		this.text = text;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}

	@Override
	public int resolveX(int guiWidth, int guiHeight) {
		if(text instanceof GUILargeText) {
			return super.resolveX(guiWidth, guiHeight) - text.getText().length() * 4 + xOffset;
		} else {
			return super.resolveX(guiWidth, guiHeight) - text.getText().length() / 2 + xOffset;
		}
	}

	@Override
	public int resolveY(int guiWidth, int guiHeight) {
		return super.resolveY(guiWidth, guiHeight) + yOffset;
	}
}
