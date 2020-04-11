package ai.arcblroth.boss.engine.tile;

import ai.arcblroth.boss.engine.Direction;
import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.TilePosition;
import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.util.StaticDefaults;

public final class EmptyFloorTile extends FloorTile {

	private static final Texture TEXTURE = StaticDefaults.DEFAULT_TEXTURE;

	public EmptyFloorTile(Room room, TilePosition pos) {
		super(room, pos, TEXTURE);
	}

	@Override
	public void onEntityHit(IEntity e, Direction s) {}

	@Override
	public void onEntityStep(IEntity e) {}

	@Override
	public void onPlayerInteract(Keybind keybind) {}

	@Override
	public String getId() {
		return "empty";
	}

	@Override
	public double getViscosity() {
		return 0;
	}

}
