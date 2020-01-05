package ai.arcblroth.boss.engine.tile;

import java.util.Random;

import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.util.StaticDefaults;

public final class EmptyFloorTile extends FloorTile {

	private static final Texture TEXTURE = StaticDefaults.DEFAULT_TEXTURE;

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
