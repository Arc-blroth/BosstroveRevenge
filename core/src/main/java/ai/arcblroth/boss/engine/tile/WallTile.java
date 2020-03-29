package ai.arcblroth.boss.engine.tile;

import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.TilePosition;
import ai.arcblroth.boss.engine.IInteractable.Direction;
import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.engine.hitbox.Hitbox;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.register.IRegistrable;
import ai.arcblroth.boss.render.Texture;

public class WallTile implements ITile, IRegistrable<WallTile> {

	private Room room;
	private TilePosition pos;
	private Texture texture;

	public WallTile(Room room, TilePosition pos, Texture t) {
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

	@Override
	public Texture getTexture() {
		return texture;
	}
	
	@Override
	public boolean isPassable() {
		return false;
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
