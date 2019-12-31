package ai.arcblroth.boss.engine.tile;

import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.Texture;

public final class EmptyWallTile extends WallTile {

	private static final Texture TEXTURE;
	static {
		int size = 16;
		TEXTURE = new Texture(size, size);
		for(int y = 0; y < size; y++) {
			for(int x = 0; x < size; x++) {
				TEXTURE.setPixel(x, y, Color.TRANSPARENT);
			}
		}
	}

	public EmptyWallTile() {
		super(TEXTURE);
	}

	@Override
	public void onEntityHit(IEntity e, Side s) {}

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
