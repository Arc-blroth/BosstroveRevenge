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
		int xTileOff = (int)Math.floor(xOffset / StaticDefaults.TILE_WIDTH);
		int yTileOff = (int)Math.floor(yOffset / StaticDefaults.TILE_WIDTH);
		int xSubtileOff = xTileOff % StaticDefaults.TILE_WIDTH;
		int ySubtileOff = yTileOff % StaticDefaults.TILE_WIDTH;
		
		return ptg;
	}

}
