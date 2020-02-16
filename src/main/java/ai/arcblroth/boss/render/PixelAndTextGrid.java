package ai.arcblroth.boss.render;

import java.util.ArrayList;

import ai.arcblroth.boss.util.StaticDefaults;
import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.util.Grid2D;
import ai.arcblroth.boss.util.Pair;

public class PixelAndTextGrid extends PixelGrid {
	
	private Grid2D<Character> textGrid;
	private Grid2D<Color> textColorGrid;
	
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
		this.textGrid = new Grid2D<Character>(width, height / 2, StaticDefaults.RESET_CHAR);
		this.textColorGrid = new Grid2D<Color>(width, height, BosstrovesRevenge.instance().getResetColor());
	}

	public ArrayList<Character> getCharacterRow(int rowNum) {
		return textGrid.getRow(rowNum / 2);
	}
	public ArrayList<Color> getCharacterColorRow(int rowNum) {
		return textColorGrid.getRow(rowNum);
	}

	public void setCharacterRow(int rowNum, ArrayList<Character> characterRow) {
		if(characterRow.size() != getWidth()) throw new IllegalArgumentException("Character row must be of the same width as the PixelGrid");
		textGrid.setRow(rowNum / 2, characterRow);
	}

	public void setCharacterRow(int rowNum, ArrayList<Character> characterRow, Color back, Color fore) {
		if(characterRow.size() != getWidth()) throw new IllegalArgumentException("Character row must be of the same width as the PixelGrid");
		textGrid.setRow(rowNum / 2, characterRow);
		for(int col = 0; col < getWidth(); col++) {
			if(characterRow.get(col) != StaticDefaults.RESET_CHAR) {
				textColorGrid.getRow(rowNum / 2 * 2).set(col, fore);
				textColorGrid.getRow(rowNum / 2 * 2 + 1).set(col, back);
			}
		}
	}

	public char getCharacterAt(int x, int y) {
		y /= 2;
		if (isTextCoordinateValid(x, y))
			return textGrid.get(x, y);
		else
			return StaticDefaults.RESET_CHAR;
	}
	
	public void setCharacter(int x, int y, char c) {
		y /= 2;
		textGrid.set(x, y, c);
	}

	public void setCharacterAt(int x, int y, char c, Color back, Color fore) {
		y /= 2;
		if (isTextCoordinateValid(x, y)) {
			textGrid.set(x, y, c);
			//This makes clever and kinda bad use of integer division
			setPixel(x, y / 2 * 2, fore);
			setPixel(x, y / 2 * 2 + 1, back);
		}
	}
	
	public Pair<Color, Color> getColorsAt(int x, int y) {
		if (isTextCoordinateValid(x, y))
			return new Pair<Color, Color>(
					textColorGrid.get(x, y / 2 * 2),
					textColorGrid.get(x, y / 2 * 2 + 1));
		else
			return new Pair<Color, Color>(BosstrovesRevenge.instance().getResetColor(), BosstrovesRevenge.instance().getResetColor());
	}

	private boolean isTextCoordinateValid(int x, int y) {
		if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight())
			return true;
		else
			return false;
	}

}
