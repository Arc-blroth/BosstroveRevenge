package ai.arcblroth.boss.game;

import ai.arcblroth.boss.render.IRenderer;
import ai.arcblroth.boss.render.PixelAndTextGrid;

public class WorldRenderer implements IRenderer {

	@Override
	public PixelAndTextGrid render() {
		return new PixelAndTextGrid(1, 1);
	}

}
