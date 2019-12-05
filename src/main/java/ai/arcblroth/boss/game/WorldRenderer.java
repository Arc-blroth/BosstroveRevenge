package ai.arcblroth.boss.game;

import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.render.IRenderer;
import ai.arcblroth.boss.render.PixelAndTextGrid;
import ai.arcblroth.boss.util.StaticDefaults;

public class WorldRenderer implements IRenderer {

	private Room room;
	private double xOffset;
	private double yOffset;

	public WorldRenderer(Room r) {
		this.room = r;
	}
	
	public void setRenderOffset(int x, int y) {
		xOffset = x;
		yOffset = y;
	}
	
	@Override
	public PixelAndTextGrid render() {
		PixelAndTextGrid ptg = new PixelAndTextGrid(StaticDefaults.OUTPUT_WIDTH, StaticDefaults.OUTPUT_HEIGHT);
		return ptg;
	}

}
