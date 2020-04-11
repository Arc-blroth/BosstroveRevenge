package ai.arcblroth.boss.util;

import java.util.Arrays;
import java.util.List;

public class Grid2D<T> {
	
	private T[][] grid;
	private int width, height;
	private T defaultElement;

	@SuppressWarnings("unchecked")
	public Grid2D(int width, int height, T defaultElement) {
		this.width = width;
		this.height = height;
		this.defaultElement = defaultElement;
		
		if(width < 0 || height < 0) throw new IllegalArgumentException("Grid width and height must be >1");

		grid = (T[][]) new Object[height][width];

		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				grid[y][x] = defaultElement;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Grid2D(Grid2D<T> copyGrid) {
		this.width = copyGrid.width;
		this.height = copyGrid.height;
		this.defaultElement = copyGrid.defaultElement;

		grid = (T[][]) new Object[height][width];

		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				grid[y][x] = copyGrid.get(x, y);
			}
		}
	}

	public T get(int x, int y) {
		checkBounds(x, y);
		return grid[y][x];
	}

	public T getOrNull(int x, int y) {
		return checkBoundsNicely(x, y) ? grid[y][x] : null;
	}
	
	public List<T> getRow(int y) {
		checkBounds(0, y);
		return Arrays.asList(grid[y]);
	}

	@SuppressWarnings("unchecked")
	public void setRow(int y, List<T> row) {
		checkBounds(0, y);
		grid[y] = (T[]) row.toArray();
	}
	
	public T set(int x, int y, T element) {
		checkBounds(x, y);
		return grid[y][x] = element == null ? defaultElement : element;
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