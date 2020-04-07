package ai.arcblroth.boss.engine.gui;

import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.render.PixelAndTextGrid;

public abstract class GUIComponent {

	private boolean visible = true;

	public abstract void render(PixelAndTextGrid target);

	public abstract void onInput(Keybind k);

	public final boolean isVisible() {
		return this.visible;
	}

	public final boolean isHidden() {
		return !this.visible;
	}

	public final void setVisible(boolean visible) {
		this.visible = visible;
	}
	
}
