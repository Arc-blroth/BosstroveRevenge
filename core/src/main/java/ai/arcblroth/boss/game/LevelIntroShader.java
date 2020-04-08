package ai.arcblroth.boss.game;

import ai.arcblroth.boss.engine.IShader;
import ai.arcblroth.boss.engine.Level;
import ai.arcblroth.boss.engine.gui.CenteredTextGUIConstraints;
import ai.arcblroth.boss.engine.gui.GUIPanel;
import ai.arcblroth.boss.engine.gui.GUIText;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.PixelAndTextGrid;
import ai.arcblroth.boss.util.CubicBezier;
import ai.arcblroth.boss.util.StaticDefaults;
import ai.arcblroth.boss.util.TextureUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class LevelIntroShader implements IShader {

	private static final CubicBezier easeIn = new CubicBezier(0.7, 0.95);
	private static final CubicBezier easeOut = new CubicBezier(0.05, 0.3);

	private Level level;
	private boolean inOrOut;
	private int timer;
	private GUIPanel panel;
	private GUIText levelNameText;
	private GUIText levelNumberText;

	public LevelIntroShader(Level level, boolean inOrOut) {
		this.level = level;
		this.timer = 0;
		this.inOrOut = inOrOut;
		this.panel = new GUIPanel(level.getIntroBackgroundColor(), Color.TRANSPARENT, 0);
		this.levelNameText = new GUIText(level.getTitleString(),
				Color.TRANSPARENT, level.getIntroForegroundColor());
		this.levelNumberText = new GUIText(
				String.format("World %s | Level %s", level.getWorldString(), level.getLevelString()),
				Color.TRANSPARENT, level.getIntroForegroundColor());
		panel.add(levelNameText, new CenteredTextGUIConstraints(levelNameText, 1, 0, -3, 1));
		panel.add(levelNumberText, new CenteredTextGUIConstraints(levelNumberText, 1, 0, 3, 1));
	}

	public void advanceFrame() {
		if(timer < StaticDefaults.LEVEL_INTRO_ANIMATION_LENGTH) timer++;
	}

	@Override
	public void render(PixelAndTextGrid target) {
		double progress = !inOrOut
				? easeIn.calculate((double)timer / StaticDefaults.LEVEL_INTRO_ANIMATION_LENGTH)
				: easeOut.calculate((double)timer / StaticDefaults.LEVEL_INTRO_ANIMATION_LENGTH);
		int textMiddleX = (int)Math.round(target.getWidth() * (!inOrOut ? -(1 - progress) + 0.5 : progress + 0.5));
		PixelAndTextGrid subTarget = TextureUtils.buildFilledTextGrid(target.getWidth(), target.getHeight(), level.getIntroBackgroundColor());
		Character[] rowOfNothing = new Character[target.getWidth()];
		Arrays.fill(rowOfNothing, ' ');
		for(int y = 0; y < subTarget.getHeight(); y += 2) {
			subTarget.setCharacterRow(y, new ArrayList<>(Arrays.asList(rowOfNothing.clone())), level.getIntroBackgroundColor(), level.getIntroBackgroundColor());
		}
		panel.render(subTarget);
		TextureUtils.overlay(subTarget, target, textMiddleX - target.getWidth() / 2, 0);
	}

}
