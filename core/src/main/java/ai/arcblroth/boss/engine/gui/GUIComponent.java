package ai.arcblroth.boss.engine.gui;

import ai.arcblroth.boss.render.PixelAndTextGrid;

public abstract class GUIComponent {
	
	public abstract void render(PixelAndTextGrid target);

	public abstract void onInput(Character c);
	
}
