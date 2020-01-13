package ai.arcblroth.boss.game;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.tile.FloorTile;
import ai.arcblroth.boss.engine.tile.WallTile;
import ai.arcblroth.boss.render.IRenderer;
import ai.arcblroth.boss.render.PixelAndTextGrid;
import ai.arcblroth.boss.util.StaticDefaults;
import ai.arcblroth.boss.util.TextureUtils;

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
		renderTiles(ptg);
		return ptg;
	}
	
	private void renderTiles(PixelAndTextGrid ptg) {
		int xTileOff = (int)Math.floor(xOffset / StaticDefaults.TILE_WIDTH);
		int yTileOff = (int)Math.floor(yOffset / StaticDefaults.TILE_HEIGHT);
		int xSubtileOff = (int)Math.round(xOffset - xTileOff * StaticDefaults.TILE_WIDTH);
		int ySubtileOff = (int)Math.round(yOffset - yTileOff * StaticDefaults.TILE_HEIGHT);
		
		//x and y are in tile units
		for(int y = 0; y < Math.ceil(StaticDefaults.OUTPUT_HEIGHT / StaticDefaults.TILE_HEIGHT) + 1; y++) {
			for(int x = 0; x < Math.ceil(StaticDefaults.OUTPUT_WIDTH / StaticDefaults.TILE_WIDTH) + 1; x++) {
				WallTile wallTile = room.getWallTiles().getOrNull(x + xTileOff, y + yTileOff);
				FloorTile floorTile = room.getFloorTiles().getOrNull(x + xTileOff, y + yTileOff);
				if(floorTile != null) {
					// pixelX and pixelY are in pixels
					// Note: we iterate from the bottomost pixel to the first
					// so that textures larger than 8x8 will extend up. 
					for(int pixelY = floorTile.getTexture().getHeight(); pixelY >= 0; pixelY--) {
						for(int pixelX = floorTile.getTexture().getWidth(); pixelX >= 0; pixelX--) {
							ptg.setPixel(
									x * StaticDefaults.TILE_WIDTH - xSubtileOff + pixelX,
									y * StaticDefaults.TILE_HEIGHT - ySubtileOff + pixelY,
									floorTile.getTexture().getPixel(pixelX, pixelY));
						}
					}
					for(int pixelY = wallTile.getTexture().getHeight(); pixelY >= 0; pixelY--) {
						for(int pixelX = wallTile.getTexture().getWidth(); pixelX >= 0; pixelX--) {
							ptg.setPixel(
									x * StaticDefaults.TILE_WIDTH - xSubtileOff + pixelX,
									y * StaticDefaults.TILE_HEIGHT - ySubtileOff + pixelY,
									TextureUtils.interpolateRGB(
											ptg.getPixel(
													x * StaticDefaults.TILE_WIDTH - xSubtileOff + pixelX,
													y * StaticDefaults.TILE_HEIGHT - ySubtileOff + pixelY), 
											wallTile.getTexture().getPixel(pixelX, pixelY),
											wallTile.getTexture().getPixel(pixelX, pixelY).getAlpha() / 255D
									)
							);
						}
					}
				}
			}
		}
	}

}
