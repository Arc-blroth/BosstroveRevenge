package ai.arcblroth.boss.game;

import ai.arcblroth.boss.engine.IShader;
import ai.arcblroth.boss.engine.Level;
import ai.arcblroth.boss.engine.gui.CenteredTextGUIConstraints;
import ai.arcblroth.boss.engine.gui.GUILargeText;
import ai.arcblroth.boss.engine.gui.GUIPanel;
import ai.arcblroth.boss.engine.gui.GUIText;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.PixelAndTextGrid;
import ai.arcblroth.boss.util.CubicBezier;
import ai.arcblroth.boss.util.StaticDefaults;
import ai.arcblroth.boss.util.TextureUtils;

public class LevelIntroShader implements IShader {

	private static final CubicBezier easeIn = new CubicBezier(0.8, 1);
	private static final CubicBezier easeOut = new CubicBezier(0, 0.2);

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
		this.levelNameText = new GUILargeText(level.getTitleString(), Color.TRANSPARENT, level.getIntroForegroundColor());
		this.levelNumberText = new GUIText(
				String.format("World %s | Level %s", level.getWorldString(), level.getLevelString()),
				Color.TRANSPARENT, level.getIntroForegroundColor());
		panel.add(levelNameText, new CenteredTextGUIConstraints(levelNameText, 4, 0, -3, 1));
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
		int textMiddleX = (int)Math.round(target.getWidth() * (!inOrOut ? progress - 0.5 : progress + 0.5));
		Color bgColor = level.getIntroBackgroundColor();
		PixelAndTextGrid subTarget = TextureUtils.buildFilledTextGrid(target.getWidth(), target.getHeight(), bgColor);
		panel.render(subTarget);

		// Hide any GUI below the intro shader
		for(int y = 0; y < subTarget.getHeight() / 2 * 2; y += 2) {
			for(int x = 0; x < subTarget.getWidth(); x++) {
				if(subTarget.get(x, y).equals(bgColor) && subTarget.get(x, y + 1).equals(bgColor) && subTarget.getCharacterAt(x, y) == StaticDefaults.RESET_CHAR) {
					subTarget.setCharacterAt(x, y, ' ', bgColor, bgColor);
				}
			}
		}

		TextureUtils.overlay(subTarget, target, textMiddleX - target.getWidth() / 2, 0);
	}

}
