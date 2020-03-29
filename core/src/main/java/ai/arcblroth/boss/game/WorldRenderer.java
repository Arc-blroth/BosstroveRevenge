package ai.arcblroth.boss.game;

import java.util.*;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.engine.IShader;
import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.engine.gui.GUI;
import ai.arcblroth.boss.engine.hitbox.Hitbox;
import ai.arcblroth.boss.engine.tile.FloorTile;
import ai.arcblroth.boss.engine.tile.WallTile;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.engine.IRenderer;
import ai.arcblroth.boss.render.PixelAndTextGrid;
import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.util.Pair;
import ai.arcblroth.boss.util.StaticDefaults;
import ai.arcblroth.boss.util.TextureUtils;

public class WorldRenderer implements IRenderer {

	private boolean renderEntityHitboxes = false;
	
	private Room room;
	private GUI gui;
	private TreeMap<Integer, IShader> worldShaders;
	private TreeMap<Integer, IShader> globalShaders;
	private double xOffset;
	private double yOffset;

	public WorldRenderer(Room r, GUI gui) {
		this.room = r;
		this.gui = gui;

		this.worldShaders = new TreeMap<>();
		this.globalShaders = new TreeMap<>();
	}
	
	@Override
	public PixelAndTextGrid render() {
		
		BosstrovesRevenge.instance().getTextureCache().stepAnimatedTextures();
		
		Pair<Integer, Integer> outputSize = BosstrovesRevenge.instance().getOutputSize();
		PixelAndTextGrid ptg = new PixelAndTextGrid(outputSize.getFirst(), outputSize.getSecond());
		renderMap(ptg);
		worldShaders.forEach((key, value) -> value.render(ptg));
		gui.render(ptg);
		globalShaders.forEach((key, value) -> value.render(ptg));
		return ptg;
	}
	
	private void renderMap(PixelAndTextGrid ptg) {
		
		TreeMap<Integer, LinkedList<IEntity>> renderLayerEntity = new TreeMap<>();
		
		Hitbox screenBounds = new Hitbox(
				Math.floor(xOffset / StaticDefaults.TILE_WIDTH) - 1,
				Math.floor(yOffset / StaticDefaults.TILE_HEIGHT) - 1,
				Math.ceil(ptg.getWidth() / (double)StaticDefaults.TILE_WIDTH) + 1,
				Math.ceil(ptg.getHeight() / (double)StaticDefaults.TILE_HEIGHT) + 1
		);
		
		for(IEntity ent : room.getEntities()) {
			Hitbox entBox = ent.getHitbox();
			if(entBox.intersects(screenBounds)) {
				int layer = (int)Math.ceil(entBox.getY() - yOffset / StaticDefaults.TILE_HEIGHT);
				if(!renderLayerEntity.containsKey(layer)) {
					renderLayerEntity.put(layer, new LinkedList<IEntity>());
				}
				renderLayerEntity.get(layer).add(ent);
			}
		}
		{
			int layer = (int)Math.ceil(room.getPlayer().getHitbox().getY() - yOffset / StaticDefaults.TILE_HEIGHT);
			if(!renderLayerEntity.containsKey(layer)) {
				renderLayerEntity.put(layer, new LinkedList<IEntity>());
			}
			renderLayerEntity.get(layer).add(room.getPlayer());
		}
		
		int xTileOff = (int) Math.floor(xOffset / StaticDefaults.TILE_WIDTH);
		int yTileOff = (int) Math.floor(yOffset / StaticDefaults.TILE_HEIGHT);
		int xSubtileOff = (int) Math.round(xOffset - xTileOff * StaticDefaults.TILE_WIDTH);
		int ySubtileOff = (int) Math.round(yOffset - yTileOff * StaticDefaults.TILE_HEIGHT);

		// x and y are in tile units
		for (int y = 0; y < Math.ceil((double)ptg.getHeight() / StaticDefaults.TILE_HEIGHT) + 1; y++) {
			for (int x = 0; x < Math.ceil((double)ptg.getWidth() / StaticDefaults.TILE_WIDTH) + 1; x++) {
				renderFloorTile(
						room.getFloorTiles().getOrNull(x + xTileOff, y + yTileOff),
						xSubtileOff, ySubtileOff, x, y, ptg);
			}
		}
		for (int y = 0; y < Math.ceil((double)ptg.getHeight() / StaticDefaults.TILE_HEIGHT) + 1; y++) {
			for (int x = 0; x < Math.ceil((double)ptg.getWidth() / StaticDefaults.TILE_WIDTH) + 1; x++) {
				renderWallTile(
						room.getWallTiles().getOrNull(x + xTileOff, y + yTileOff),
						xSubtileOff, ySubtileOff, x, y, ptg);
			}
			if(renderLayerEntity.containsKey(y)) {
				renderLayerEntity.get(y).forEach(ent -> renderEntity(ptg, ent));
			}
		}
		
	}
	
	private void renderFloorTile(FloorTile floorTile, int xSubtileOff, int ySubtileOff, int x, int y, PixelAndTextGrid ptg) {
		if(floorTile != null) {
			// pixelX and pixelY are in pixels
			// Note: we iterate from the bottomost pixel to the first
			// so that textures larger than 8x8 will extend up.
			for (int pixelY = floorTile.getTexture().getHeight() - 1; pixelY >= 0; pixelY--) {
				for (int pixelX = floorTile.getTexture().getWidth() - 1; pixelX >= 0; pixelX--) {
					ptg.setPixel(
							(x + 1) * StaticDefaults.TILE_WIDTH - xSubtileOff - floorTile.getTexture().getWidth() + pixelX,
							(y + 1) * StaticDefaults.TILE_HEIGHT - ySubtileOff - floorTile.getTexture().getHeight() + pixelY,
							floorTile.getTexture().getPixel(pixelX, pixelY)
					);
				}
			}
		}
	}
	
	private void renderWallTile(WallTile wallTile, int xSubtileOff, int ySubtileOff, int x, int y, PixelAndTextGrid ptg) {
		if(wallTile != null) {
			for (int pixelY = wallTile.getTexture().getHeight() - 1; pixelY >= 0; pixelY--) {
				for (int pixelX = wallTile.getTexture().getWidth() - 1; pixelX >= 0; pixelX--) {
					ptg.setPixel(
							(x + 1) * StaticDefaults.TILE_WIDTH - xSubtileOff - wallTile.getTexture().getWidth()  + pixelX,
							(y + 1) * StaticDefaults.TILE_HEIGHT - ySubtileOff - wallTile.getTexture().getHeight() + pixelY,
							TextureUtils.interpolateRGB(
									ptg.getPixel(
											(x + 1) * StaticDefaults.TILE_WIDTH - xSubtileOff - wallTile.getTexture().getWidth() + pixelX,
											(y + 1) * StaticDefaults.TILE_HEIGHT - ySubtileOff - wallTile.getTexture().getHeight() + pixelY
									),
									wallTile.getTexture().getPixel(pixelX, pixelY),
									wallTile.getTexture().getPixel(pixelX, pixelY).getAlpha() / 255D
							)
					);
				}
			}
		}
	}
	
	private void renderEntity(PixelAndTextGrid ptg, IEntity ent) {
		Texture entTexture = ent.getTexture();
		
		// Calculate upper-left corner pos
		int xEntityOff = (int)Math.round(
				((double)ent.getHitbox().getX() + (double)ent.getHitbox().getWidth() / 2D) * StaticDefaults.TILE_WIDTH
				- xOffset - entTexture.getWidth() / 2D
		);
		int yEntityOff = (int)Math.round(
				((double)ent.getHitbox().getY() + (double)ent.getHitbox().getHeight() / 2D) * StaticDefaults.TILE_HEIGHT
				- yOffset -  entTexture.getHeight() / 2D
		);
		
		// Draw entity texture pixels onto pixelgrid
		for (int pixelY = 0; pixelY < entTexture.getHeight(); pixelY++) {
			for (int pixelX = 0; pixelX < entTexture.getWidth(); pixelX++) {
				ptg.setPixel(
						xEntityOff + pixelX,
						yEntityOff + pixelY,
						TextureUtils.interpolateRGB(
								ptg.getPixel(
										xEntityOff + pixelX,
										yEntityOff + pixelY
								),
								entTexture.getPixel(pixelX, pixelY),
								entTexture.getPixel(pixelX, pixelY).getAlpha() / 255D
						)
				);
			}
		}
		
		if(renderEntityHitboxes) {
			int xHitboxOff = (int)Math.round(
					((double)ent.getHitbox().getX()) * StaticDefaults.TILE_WIDTH
					- xOffset
			);
			int yHitboxOff = (int)Math.round(
					((double)ent.getHitbox().getY()) * StaticDefaults.TILE_HEIGHT
					- yOffset
			);
			Random rand = new Random(ent.hashCode());
			Color hitColor = Color.getFromHSBA(rand.nextDouble(), 1, 1);
			for (int pixelY = 0; pixelY < ent.getHitbox().getHeight() * StaticDefaults.TILE_HEIGHT; pixelY++) {
				for (int pixelX = 0; pixelX < ent.getHitbox().getWidth() * StaticDefaults.TILE_WIDTH; pixelX++) {
					ptg.setPixel(
							xHitboxOff + pixelX,
							yHitboxOff + pixelY,
							hitColor
					);
				}
			}
		}
	}

	public void putWorldShader(int priority, IShader shader) {
		while(worldShaders.containsKey(priority)) {
			priority++;
		}
		worldShaders.put(priority, shader);
	}

	public void removeWorldShader(IShader shader) {
		worldShaders.entrySet().removeIf(entry -> shader.equals(entry.getValue()));
	}

	public void putGlobalShader(int priority, IShader shader) {
		while(globalShaders.containsKey(priority)) {
			priority++;
		}
		globalShaders.put(priority, shader);
	}

	public void removeGlobalShader(IShader shader) {
		globalShaders.entrySet().removeIf(entry -> shader.equals(entry.getValue()));
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

}
