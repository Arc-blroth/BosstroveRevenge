package ai.arcblroth.boss.game.cutscene;

import ai.arcblroth.boss.engine.IShader;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.PixelAndTextGrid;

import java.util.ArrayList;
import java.util.Arrays;

public class TopAndBottomBarShader implements IShader {

	private Color barColor;
	private double topBarHeight;
	private double bottomBarHeight;

	public TopAndBottomBarShader(Color barColor, double topBarHeight, double bottomBarHeight) {
		this.barColor = barColor;
		this.topBarHeight = topBarHeight;
		this.bottomBarHeight = bottomBarHeight;
	}

	@Override
	public void render(PixelAndTextGrid target) {
		Character[] rowOfSpaces = new Character[target.getWidth()];
		Arrays.fill(rowOfSpaces, ' ');
		Color[] rowOfColor = new Color[target.getWidth()];
		Arrays.fill(rowOfColor, barColor);
		int topRows = Math.min(Math.max((int)Math.round(topBarHeight * target.getHeight()), 0), target.getHeight());
		int bottomRows = Math.min(Math.max((int)Math.round(bottomBarHeight * target.getHeight()), 0), target.getHeight() - topRows);
		for(int y = 0; y < topRows; y++) {
			if(y % 2 == 0) target.setCharacterRow(y, new ArrayList<>(Arrays.asList(rowOfSpaces.clone())), barColor, barColor);
			target.setRow(y, Arrays.asList(rowOfColor.clone()));
		}
		for(int y =target.getHeight() - bottomRows; y < target.getHeight(); y++) {
			if(y % 2 == 0) target.setCharacterRow(y, new ArrayList<>(Arrays.asList(rowOfSpaces.clone())), barColor, barColor);
			target.setRow(y, Arrays.asList(rowOfColor.clone()));
		}
	}

	public Color getBarColor() {
		return barColor;
	}

	public void setBarColor(Color barColor) {
		this.barColor = barColor;
	}

	public double getTopBarHeight() {
		return topBarHeight;
	}

	public void setTopBarHeight(double topBarHeight) {
		this.topBarHeight = topBarHeight;
	}

	public double getBottomBarHeight() {
		return bottomBarHeight;
	}

	public void setBottomBarHeight(double bottomBarHeight) {
		this.bottomBarHeight = bottomBarHeight;
	}

}
