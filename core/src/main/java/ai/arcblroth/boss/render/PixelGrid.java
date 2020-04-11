package ai.arcblroth.boss.render;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.util.Grid2D;

public class PixelGrid extends Grid2D<Color> {

	public PixelGrid(int width, int height) {
		super(width, height, BosstrovesRevenge.instance().getResetColor());
	}
	
	public PixelGrid(Grid2D<Color> copyGrid) {
		super(copyGrid);
	}

	public Color getPixel(int x, int y) {
		if (isCoordinateValid(x, y))
			return get(x, y);
		else
			return BosstrovesRevenge.instance().getResetColor();
	}

	public void setPixel(int x, int y, Color c) {
		if (isCoordinateValid(x, y))
			set(x, y, c);
	}

	protected boolean isCoordinateValid(int x, int y) {
		if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight())
			return true;
		else
			return false;
	}

}
