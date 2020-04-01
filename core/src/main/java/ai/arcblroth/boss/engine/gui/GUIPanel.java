package ai.arcblroth.boss.engine.gui;

import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.PixelAndTextGrid;
import ai.arcblroth.boss.util.TextureUtils;

public class GUIPanel extends GUIParent {

	private Color backgroundColor;
	private Color borderColor;
	private int borderWidth;
	
	/**
	 * Creates a new GUIPanel with a transparent background and border.
	 */
	public GUIPanel() {
		this.backgroundColor = Color.TRANSPARENT;
		this.borderColor = Color.TRANSPARENT;
		this.borderWidth = 0;
	}
	
	/**
	 * Creates a new GUIPanel with the specified background and border colors.
	 * @param backgroundColor background color of this panel
	 * @param borderColor border color of this panel
	 * @param borderWidth width of border in pixels
	 */
	public GUIPanel(Color backgroundColor, Color borderColor, int borderWidth) {
		this.backgroundColor = backgroundColor;
		this.borderColor = borderColor;
		this.borderWidth = borderWidth;
	}
	
	@Override
	public void render(PixelAndTextGrid target) {
		for(int y = 0; y < target.getHeight(); y++) {
			for(int x = 0; x < target.getWidth(); x++) {
				if(x <= borderWidth - 1 || y <= borderWidth - 1 || x >= target.getWidth() - borderWidth || y >= target.getHeight() - borderWidth) {
					target.set(x, y, borderColor);
				} else {
					target.set(x, y, backgroundColor);
				}
			}
		}
		if(target.getWidth() - 2 * borderWidth > 0 && target.getHeight() - 2 * borderWidth > 0) {
			PixelAndTextGrid nonborderedTarget = buildTransparentGrid(target.getWidth() - 2 * borderWidth, target.getHeight() - 2 * borderWidth);
			super.render(nonborderedTarget);
			TextureUtils.overlay(nonborderedTarget, target, borderWidth, (int)Math.ceil(borderWidth / 2D) * 2);
		}
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public Color getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	public int getBorderWidth() {
		return borderWidth;
	}

	public void setBorderWidth(int borderWidth) {
		this.borderWidth = borderWidth;
	}
}
