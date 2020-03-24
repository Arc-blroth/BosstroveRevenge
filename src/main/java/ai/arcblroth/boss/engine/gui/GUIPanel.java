package ai.arcblroth.boss.engine.gui;

import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.PixelAndTextGrid;

public class GUIPanel extends GUIParent {
	
	private Color backgroundColor;
	private Color borderColor;
	
	/**
	 * Creates a new GUIPanel with a transparent background and border.
	 */
	public GUIPanel() {
		this.backgroundColor = Color.TRANSPARENT;
		this.borderColor = Color.TRANSPARENT;
	}
	
	/**
	 * Creates a new GUIPanel with the specified background and border colors.
	 * @param backgroundColor background color of this panel
	 * @param borderColor border color of this panel
	 */
	public GUIPanel(Color backgroundColor, Color borderColor) {
		this.backgroundColor = backgroundColor;
		this.borderColor = borderColor;
	}
	
	@Override
	public void render(PixelAndTextGrid target) {
		for(int y = 0; y < target.getHeight(); y++) {
			for(int x = 0; x < target.getWidth(); x++) {
				if(x == 0 || y == 0 || x == target.getWidth() - 1 || y == target.getHeight() - 1) {
					target.set(x, y, borderColor);
				} else {
					target.set(x, y, backgroundColor);
				}
			}
		}
		super.render(target);
	}

}
