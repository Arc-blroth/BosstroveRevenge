package ai.arcblroth.boss.engine.gui;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.PixelAndTextGrid;
import ai.arcblroth.boss.resource.load.TextureCache;
import ai.arcblroth.boss.util.TextureUtils;

import static ai.arcblroth.boss.resource.load.CharacterTextureLoader.TEXTURE_HEIGHT;
import static ai.arcblroth.boss.resource.load.CharacterTextureLoader.TEXTURE_WIDTH;

public class GUILargeText extends GUIText {

	public GUILargeText(CharSequence text) {
		super(text);
	}

	public GUILargeText(CharSequence text, Color backgroundColor, Color foregroundColor) {
		super(text, backgroundColor, foregroundColor);
	}

	public GUILargeText(CharSequence text, Color backgroundColor, Color foregroundColor, boolean wordWrap) {
		super(text, backgroundColor, foregroundColor, wordWrap);
	}

	@Override
	public void render(PixelAndTextGrid target) {
		// We only support english :)
		Color backgroundColor = getBackgroundColor();
		Color foregroundColor = getForegroundColor();
		TextureCache cache = BosstrovesRevenge.instance().getTextureCache();
		if(isWordWrapping()) {
			String[] words = getText().split(" ");

			int lastX = -TEXTURE_WIDTH;
			int lastY = 0;
			for(String word : words) {
				//If a space and the word fits in the remaining space on this line
				if(lastX + TEXTURE_WIDTH * (1 + word.length()) < target.getWidth()) {
					TextureUtils.overlay(cache.getCharacter(' '), target, lastX, lastY);
					lastX += TEXTURE_WIDTH;
					for(char mander : word.toCharArray()) {
						TextureUtils.overlay(cache.getCharacter(mander), target, lastX, lastY);
						lastX += TEXTURE_WIDTH;
					}
				} else {
					//Is there a next line?
					if(lastY + TEXTURE_HEIGHT < target.getHeight() - 1) {
						//Does the word fit completely on the next line?
						if(word.length() * TEXTURE_WIDTH <= target.getWidth()) {
							lastX = 0;
							lastY += TEXTURE_HEIGHT;
							for(char meleon : word.toCharArray()) {
								TextureUtils.overlay(cache.getCharacter(meleon), target, lastX, lastY);
								lastX += TEXTURE_WIDTH;
							}
						} else {
							//Fine, we'll break it up
							lastX = -TEXTURE_WIDTH;
							lastY += TEXTURE_HEIGHT;
							for(char izard : word.toCharArray()) {
								lastX += TEXTURE_WIDTH;
								if (lastX == target.getWidth() - 2) {
									if (lastY < target.getHeight() - 1) {
										TextureUtils.overlay(cache.getCharacter('-'), target, lastX, lastY);
										lastX = 0;
										lastY += TEXTURE_HEIGHT;
									} else {
										TextureUtils.overlay(cache.getCharacter('.'), target, lastX, lastY);
										return;
									}
								}

								TextureUtils.overlay(cache.getCharacter(izard), target, lastX, lastY);
							}
						}
					}
				}
			}
		} else {
			int lastX = -TEXTURE_WIDTH;
			int lastY = 0;
			for(char jabug : getText().toCharArray()) {
				lastX += TEXTURE_WIDTH;
				if(lastX >= target.getWidth() - TEXTURE_WIDTH && lastY >= target.getHeight() - TEXTURE_HEIGHT) return;
				if(lastX >= target.getWidth() - TEXTURE_WIDTH) {
					lastX = 0;
					lastY += TEXTURE_HEIGHT;
				}

				TextureUtils.overlay(cache.getCharacter(jabug), target, lastX, lastY);
			}
		}
	}

}
