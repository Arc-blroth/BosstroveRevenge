package ai.arcblroth.boss.engine.gui;

import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.PixelAndTextGrid;

public class GUILevelBar extends GUIComponent {

	private double level;
	private Color backgroundColor;
	private Color foregroundColor;

	public GUILevelBar(double level, Color backgroundColor, Color foregroundColor) {
		this.level = level;
		this.backgroundColor = backgroundColor;
		this.foregroundColor = foregroundColor;
	}

	@Override
	public void render(PixelAndTextGrid target) {
		int whereDoesFgStop = (int)Math.round(target.getWidth() * level);
		for(int y = 0; y < target.getHeight(); y++) {
			for(int x = 0; x < whereDoesFgStop; x++) {
				target.set(x, y, foregroundColor);
			}
			for(int x = whereDoesFgStop; x < target.getWidth(); x++) {
				target.set(x, y, backgroundColor);
			}
		}
	}

	@Override
	public void onInput(Keybind k) {

	}

	public double getLevel() {
		return level;
	}

	public void setLevel(double level) {
		this.level = Math.max(Math.min(level, 1), 0);
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public Color getForegroundColor() {
		return foregroundColor;
	}

	public void setForegroundColor(Color foregroundColor) {
		this.foregroundColor = foregroundColor;
	}

}
