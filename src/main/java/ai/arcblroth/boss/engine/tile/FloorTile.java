package ai.arcblroth.boss.engine.tile;

import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.register.IRegistrable;
import ai.arcblroth.boss.render.Texture;

public class FloorTile implements ITile, IRegistrable<FloorTile> {

	private Texture texture;

	public FloorTile(Texture t) {
		this.texture = t;
	}

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
	public void onEntityHit(IEntity entity, Direction s) {
		
	}

	@Override
	public void onEntityStep(IEntity entity) {
		
	}

	@Override
	public void onPlayerInteract(Keybind keybind) {
		
	}

}
