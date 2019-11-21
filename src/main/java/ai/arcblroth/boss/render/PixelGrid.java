package ai.arcblroth.boss.render;

import java.util.concurrent.*;
import java.awt.Color;

public class PixelGrid extends ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Color>> {

	private int width, height;

	public PixelGrid(int width, int height) {
		this.width = width;
		this.height = height;

		// Init the grid to just pure black
		for (int hi = 0; hi < height; hi++) {
			ConcurrentHashMap<Integer, Color> row = new ConcurrentHashMap<Integer, Color>();
			for (int wi = 0; wi < width; wi++) {
				row.put(wi, Color.BLACK);
			}
			this.put(hi, row);
		}
	}

	public Color getPixel(int x, int y) {
		if (isCoordinateValid(x, y))
			return this.get(y).get(x);
		else
			return Color.BLACK;
	}

	public void setPixel(int x, int y, Color c) {
		if (isCoordinateValid(x, y)) {
			this.get(y).remove(x);
			this.get(y).put(x, c);
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	private boolean isCoordinateValid(int x, int y) {
		if (x >= 0 && x < width && y >= 0 && y < height)
			return true;
		else
			return false;
	}

}
