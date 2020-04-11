package ai.arcblroth.boss.engine.tile;

import ai.arcblroth.boss.engine.Direction;
import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.TilePosition;
import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.register.IRegistrable;
import ai.arcblroth.boss.render.Texture;

public abstract class FloorTile implements ITile, IRegistrable<FloorTile> {

	private Room room;
	private TilePosition pos;
	private Texture texture;

	public FloorTile(Room room, TilePosition pos, Texture t) {
		this.room = room;
		this.pos = pos;
		this.texture = t;
	}
	
	@Override
	public final Room getRoom() {
		return room;
	}
	
	@Override
	public final TilePosition getPosition() {
		return pos;
	}

	public abstract String getId();

	@Override
	public Texture getTexture() {
		return texture;
	}
	
	@Override
	public boolean isPassable() {
		return true;
	}

	@Override
	public double getViscosity() {
		return 0;
	}

	@Override
	public void onStep() {
		
	}

	@Override
	public void onEntityHit(IEntity entity, Direction s) {
		
	}

	@Override
	public void onEntityStep(IEntity entity) {
		
	}

	@Override
	public void onPlayerInteract(Keybind keybind) {
		
	}

}
