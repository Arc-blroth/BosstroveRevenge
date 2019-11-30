package ai.arcblroth.boss.render;

import java.util.ArrayList;

import ai.arcblroth.boss.consoleio.OutputDefaults;
import ai.arcblroth.boss.util.Pair;

public class PixelAndTextGrid extends PixelGrid {
	
	private ArrayList<ArrayList<Character>> textGrid;
	private ArrayList<ArrayList<Color>> textColorGrid;
	
	public PixelAndTextGrid(PixelGrid pg) {
		this(pg.getWidth(), pg.getHeight());
		
		for (int hi = 0; hi < getHeight(); hi++) {
			for (int wi = 0; wi < getWidth(); wi++) {
				this.setPixel(wi, hi, pg.getPixel(wi, hi));
			}
		}
	}
	
	public PixelAndTextGrid(int width, int height) {
		super(width, height);
		this.textGrid = new ArrayList<ArrayList<Character>>();
		this.textColorGrid = new ArrayList<ArrayList<Color>>();
		
		// Init the text grid to spaces
		for (int hi = 0; hi < height / 2; hi++) {
			ArrayList<Character> row = new ArrayList<Character>();
			for (int wi = 0; wi < width; wi++) {
				row.add(OutputDefaults.RESET_CHAR);
			}
			textGrid.add(row);
		}
		// Init the color grid to black
		for (int hi = 0; hi < height; hi++) {
			ArrayList<Color> row = new ArrayList<Color>();
			for (int wi = 0; wi < width; wi++) {
				row.add(OutputDefaults.RESET_COLOR);
			}
			textColorGrid.add(row);
		}
	}

	public ArrayList<Character> getCharacterRow(int rowNum) {
		return textGrid.get(rowNum / 2);
	}
	public ArrayList<Color> getCharacterColorRow(int rowNum) {
		return textColorGrid.get(rowNum / 2);
	}

	public void setCharacterRow(int rowNum, ArrayList<Character> characterRow) {
		if(characterRow.size() != getWidth()) throw new IllegalArgumentException("Character row must be of the same width as the PixelGrid");
		textGrid.set(rowNum / 2, characterRow);
	}

	public void setCharacterRow(int rowNum, ArrayList<Character> characterRow, Color back, Color fore) {
		if(characterRow.size() != getWidth()) throw new IllegalArgumentException("Character row must be of the same width as the PixelGrid");
		textGrid.set(rowNum / 2, characterRow);
		for(int col = 0; col < getWidth(); col++) {
			if(characterRow.get(col) != OutputDefaults.RESET_CHAR) {
				textColorGrid.get(rowNum / 2 * 2).set(col, fore);
				textColorGrid.get(rowNum / 2 * 2 + 1).set(col, back);
			}
		}
	}

	public char getCharacterAt(int x, int y) {
		y /= 2;
		if (isTextCoordinateValid(x, y))
			return textGrid.get(y).get(x);
		else
			return OutputDefaults.RESET_CHAR;
	}
	
	public void setCharacter(int x, int y, char c) {
		y /= 2;
		if (isTextCoordinateValid(x, y)) {
			textGrid.get(y).set(x, c);
		}
	}

	public void setCharacterAt(int x, int y, char c, Color back, Color fore) {
		y /= 2;
		if (isTextCoordinateValid(x, y)) {
			textGrid.get(y).set(x, c);
			//This makes clever and kinda bad use of integer division
			setPixel(x, y / 2 * 2, fore);
			setPixel(x, y / 2 * 2 + 1, back);
		}
	}
	
	public Pair<Color, Color> getColorsAt(int x, int y) {
		if (isTextCoordinateValid(x, y))
			return new Pair<Color, Color>(
					textColorGrid.get(y / 2 * 2).get(x),
					textColorGrid.get(y / 2 * 2 + 1).get(x));
		else
			return new Pair<Color, Color>(OutputDefaults.RESET_COLOR, OutputDefaults.RESET_COLOR);
	}

	private boolean isTextCoordinateValid(int x, int y) {
		if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight())
			return true;
		else
			return false;
	}

}
