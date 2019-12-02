package ai.arcblroth.boss.render;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;

import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.util.OutputDefaults;

public class PixelGrid {

	private ArrayList<ArrayList<Color>> grid;
	private int width, height;
	
	public PixelGrid(PixelGrid pg) {
		this(pg.getWidth(), pg.getHeight());
		
		for (int hi = 0; hi < getHeight(); hi++) {
			for (int wi = 0; wi < getWidth(); wi++) {
				this.setPixel(wi, hi, pg.getPixel(wi, hi));
			}
		}
	}
	
	public PixelGrid(int width, int height) {
		if(width < 1 || height < 1) throw new IllegalArgumentException("PixelGrid width and height must be >1");
		
		this.grid = new ArrayList<ArrayList<Color>>(height);
		this.width = width;
		this.height = height;

		// Init the grid to just pure black
		for (int hi = 0; hi < height; hi++) {
			ArrayList<Color> row = new ArrayList<Color>(width);
			for (int wi = 0; wi < width; wi++) {
				row.add(OutputDefaults.RESET_COLOR);
			}
			grid.add(row);
		}
	}

	public Color getPixel(int x, int y) {
		if (isCoordinateValid(x, y))
			return grid.get(y).get(x);
		else
			return OutputDefaults.RESET_COLOR;
	}

	public void setPixel(int x, int y, Color c) {
		if (isCoordinateValid(x, y)) {
			grid.get(y).set(x, c);
		}
	}

	public ArrayList<Color> getRow(int rowNum) {
		return grid.get(rowNum);
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
