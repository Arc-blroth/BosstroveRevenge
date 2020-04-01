package ai.arcblroth.boss.engine.gui;

import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.render.PixelAndTextGrid;
import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.util.TextureUtils;

public class GUIImage extends GUIComponent {

	private final Texture image;

	public GUIImage(Texture image) {
		this.image = image;
	}

	@Override
	public void render(PixelAndTextGrid target) {
		TextureUtils.overlay(image, target);
	}

	@Override
	public void onInput(Keybind k) {

	}

}
