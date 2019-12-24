package ai.arcblroth.boss.engine.tile;

import java.util.Random;

import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.in.Keybind;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.Texture;

public final class EmptyFloorTile extends FloorTile {

	private static final Texture TEXTURE;
	static {
		int size = 16;
		Random r = new Random(size);
		TEXTURE = new Texture(size, size);
		for(int y = 0; y < size; y++) {
			for(int x = 0; x < size; x++) {
				TEXTURE.setPixel(x, y, Color.getFromHSBA(r.nextDouble()/5+0.5, 0.5, 0.8));
				//TEXTURE.setPixel(x, y, Color.WHITE);
			}
		}
	}

	public EmptyFloorTile() {
		super(TEXTURE);
	}

	@Override
	public void onEntityHit(IEntity e, Side s) {}

	@Override
	public void onEntityStep(IEntity e) {}

	@Override
	public void onPlayerInteract(Keybind keybind) {}

	@Override
	public double getViscosity() {
		return 0;
	}

}
