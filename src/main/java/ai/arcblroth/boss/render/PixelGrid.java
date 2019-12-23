package ai.arcblroth.boss.render;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;

import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.util.Grid2D;
import ai.arcblroth.boss.util.StaticDefaults;

public class PixelGrid extends Grid2D<Color> {

	public PixelGrid(int width, int height) {
		super(width, height, StaticDefaults.RESET_COLOR);
	}
	
	public PixelGrid(Grid2D<Color> copyGrid) {
		super(copyGrid);
	}

	public Color getPixel(int x, int y) {
		if (isCoordinateValid(x, y))
			return get(x, y);
		else
			return StaticDefaults.RESET_COLOR;
	}

	public void setPixel(int x, int y, Color c) {
		if (isCoordinateValid(x, y))
			set(x, y, c);
	}

	private boolean isCoordinateValid(int x, int y) {
		if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight())
			return true;
		else
			return false;
	}

}
