package ai.arcblroth.boss.engine.tile;

import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.TilePosition;
import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.PixelGrid;
import ai.arcblroth.boss.render.Texture;

public final class EmptyWallTile extends WallTile {

	private static final Texture TEXTURE;
	static {
		int size = 16;
		PixelGrid pg = new PixelGrid(size, size);
		for(int y = 0; y < size; y++) {
			for(int x = 0; x < size; x++) {
				pg.setPixel(x, y, Color.TRANSPARENT);
			}
		}
		TEXTURE = new Texture(pg);
	}

	public EmptyWallTile(Room room, TilePosition pos) {
		super(room, pos, TEXTURE);
	}

	@Override
	public void onEntityHit(IEntity e, Direction s) {}

	@Override
	public void onEntityStep(IEntity e) {}

	@Override
	public void onPlayerInteract(Keybind keybind) {}

	@Override
	public boolean isPassable() {
		return true;
	}

	@Override
	public double getViscosity() {
		return 0;
	}

}
