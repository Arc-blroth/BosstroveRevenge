package ai.arcblroth.boss.engine.gui;

import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.PixelAndTextGrid;

public class GUIText extends GUIComponent {
	
	private StringBuffer textBuf;
	private Color backgroundColor;
	private Color foregroundColor;
	private boolean wordWrap;

	public GUIText(CharSequence text) {
		this(text, Color.TRANSPARENT, Color.BLACK);
	}
	
	public GUIText(CharSequence text, Color backgroundColor, Color foregroundColor) {
		this(text, backgroundColor, foregroundColor, true);
	}
	
	public GUIText(CharSequence text, Color backgroundColor, Color foregroundColor, boolean wordWrap) {
		this.textBuf = new StringBuffer(text);
		this.backgroundColor = backgroundColor;
		this.foregroundColor = foregroundColor;
		this.wordWrap = wordWrap;
	}
	
	@Override
	public void render(PixelAndTextGrid target) {
		// We only support english :)
		if(wordWrap) {
			String[] words = textBuf.toString().split(" ");

			int lastX = -1;
			int lastY = 0;
			for(String word : words) {
				//If a space and the word fits in the remaining space on this line
				if(lastX + 1 + word.length() < target.getWidth()) {
					target.setCharacterAt(lastX++, lastY, ' ', backgroundColor, foregroundColor);
					for(char mander : word.toCharArray()) {
						target.setCharacterAt(lastX++, lastY, mander, backgroundColor, foregroundColor);
					}
				} else {
					//Is there a next line?
					if(lastY + 2 < target.getHeight() - 1) {
						//Does the word fit completely on the next line?
						if(word.length() <= target.getWidth()) {
							lastX = 0;
							lastY += 2;
							for(char meleon : word.toCharArray()) {
								target.setCharacterAt(lastX++, lastY, meleon, backgroundColor, foregroundColor);
							}
						} else {
							//Fine, we'll break it up
							lastX = -1;
							lastY += 2;
							for(char izard : word.toCharArray()) {
								lastX++;
								if (lastX == target.getWidth() - 2) {
									if (lastY < target.getHeight() - 1) {
										target.setCharacterAt(lastX, lastY, '-', backgroundColor, foregroundColor);
										lastX = 0;
										lastY += 2;
									} else {
										target.setCharacterAt(lastX, lastY, '.', backgroundColor, foregroundColor);
										return;
									}
								}

								target.setCharacterAt(lastX, lastY, izard, backgroundColor, foregroundColor);
							}
						}
					}
				}
			}
		} else {
			int lastX = -1;
			int lastY = 0;
			for(char jabug : textBuf.toString().toCharArray()) {
				lastX++;
				if(lastX == target.getWidth() - 1 && lastY == target.getHeight() - 2) return;
				if(lastX == target.getWidth() - 1) {
					lastX = 0;
					lastY += 2;
				}

				target.setCharacterAt(lastX, lastY, jabug, backgroundColor, foregroundColor);
			}
		}
	}

	@Override
	public void onInput(Keybind k) {
		
	}
	
	public void appendText(CharSequence text) {
		textBuf.append(text);
	}
	
	public void setText(CharSequence text) {
		textBuf.delete(0, textBuf.length());
		textBuf.append(text);
	}
	
	public String getText() {
		return textBuf.toString();
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public Color getForegroundColor() {
		return foregroundColor;
	}

	public void setForegroundColor(Color foregroundColor) {
		this.foregroundColor = foregroundColor;
	}
	

}
