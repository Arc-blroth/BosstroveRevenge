package ai.arcblroth.boss.util;

import java.util.ArrayList;

public class Grid2D<T> {
	
	private ArrayList<ArrayList<T>> grid;
	private int width, height;
	private T defaultElement;
	
	public Grid2D(int width, int height, T defaultElement) {
		this.width = width;
		this.height = height;
		this.defaultElement = defaultElement;
		
		if(width <= 0 || height <= 0) throw new IllegalArgumentException("Grid width and height must be >1");
		
		grid = new ArrayList<ArrayList<T>>(height);
		for(int y = 0; y < height; y++) {
			ArrayList<T> row = new ArrayList<T>();
			for(int x = 0; x < width; x++) {
				row.add(defaultElement);
			}
			grid.add(row);
		}
	}
	
	public Grid2D(Grid2D<T> copyGrid) {
		this.width = copyGrid.width;
		this.height = copyGrid.height;
		
		grid = new ArrayList<ArrayList<T>>(height);
		for(int y = 0; y < height; y++) {
			ArrayList<T> row = new ArrayList<T>();
			for(int x = 0; x < width; x++) {
				row.add(copyGrid.get(x, y));
			}
			grid.add(row);
		}
	}

	public T get(int x, int y) {
		checkBounds(x, y);
		return grid.get(y).get(x);
	}

	public T getOrNull(int x, int y) {
		return checkBoundsNicely(x, y) ? grid.get(y).get(x) : null;
	}
	
	public ArrayList<T> getRow(int y) {
		checkBounds(0, y);
		return grid.get(y);
	}

	public void setRow(int y, ArrayList<T> row) {
		checkBounds(0, y);
		grid.set(y, row);
	}
	
	public T set(int x, int y, T element) {
		checkBounds(x, y);
		return grid.get(y).set(x, element == null ? defaultElement : element);
	}

	public void forEach(TriConsumer<Integer, Integer, T> consumer) {
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				consumer.accept(x, y, this.get(x, y));
			}
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	private void checkBounds(int x, int y) throws IllegalArgumentException {
		if(x < 0 || y < 0 || x >= width || y >= height) throw new IllegalArgumentException("Grid x and y out of bounds!");
	}
	
	private boolean checkBoundsNicely(int x, int y) {
		if(x < 0 || y < 0 || x >= width || y >= height) return false;
		else return true;
	}
	
}