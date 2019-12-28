package ai.arcblroth.boss.game;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.tile.FloorTile;
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
	
	public void setRenderOffset(double d, double e) {
		xOffset = d;
		yOffset = e;
	}
	

	public double getRenderOffsetX() {
		return xOffset;
	}
	
	public double getRenderOffsetY() {
		return yOffset;
	}
	
	@Override
	public PixelAndTextGrid render() {
		PixelAndTextGrid ptg = new PixelAndTextGrid(StaticDefaults.OUTPUT_WIDTH, StaticDefaults.OUTPUT_HEIGHT);
		int xTileOff = (int)Math.ceil(xOffset / StaticDefaults.TILE_WIDTH) - 1;
		int yTileOff = (int)Math.ceil(yOffset / StaticDefaults.TILE_HEIGHT) - 1;
		int xSubtileOff = (int)Math.round(xOffset % StaticDefaults.TILE_WIDTH);
		int ySubtileOff = (int)Math.round(yOffset % StaticDefaults.TILE_HEIGHT);
		
		//x and y are in tile units
		for(int y = 0; y < Math.ceil(StaticDefaults.OUTPUT_HEIGHT / StaticDefaults.TILE_HEIGHT) + 1; y++) {
			for(int x = 0; x < Math.ceil(StaticDefaults.OUTPUT_WIDTH / StaticDefaults.TILE_WIDTH) + 1; x++) {
				FloorTile floorTile = room.getFloorTiles().getOrNull(x + xTileOff, y + yTileOff);
				if(floorTile != null) {
					//pixelX and pixelY are in pixels
					for(int pixelY = 0; pixelY < StaticDefaults.TILE_HEIGHT; pixelY++) {
						for(int pixelX = 0; pixelX < StaticDefaults.TILE_WIDTH; pixelX++) {
							
							ptg.setPixel(
									x * StaticDefaults.TILE_WIDTH - xSubtileOff + pixelX,
									y * StaticDefaults.TILE_HEIGHT - ySubtileOff + pixelY,
									floorTile.getTexture().getPixel(pixelX, pixelY));
						}
					}
				}
			}
		}
		return ptg;
	}

}
