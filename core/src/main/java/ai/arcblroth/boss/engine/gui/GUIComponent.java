package ai.arcblroth.boss.engine.gui;

import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.render.PixelAndTextGrid;

public abstract class GUIComponent {

	private boolean isHidden;

	public abstract void render(PixelAndTextGrid target);

	public abstract void onInput(Keybind k);

	public final boolean isHidden() {
		return this.isHidden;
	}

	public final void setHidden(boolean hidden) {
		this.isHidden = hidden;
	}
	
}
