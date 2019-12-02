package ai.arcblroth.boss.engine.tile;

import ai.arcblroth.boss.engine.IInteractable.Side;
import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.in.Keybind;
import ai.arcblroth.boss.register.IRegistrable;
import ai.arcblroth.boss.render.Texture;

public class WallTile implements ITile, IRegistrable<WallTile> {

	private Texture texture;

	public WallTile(Texture t) {
		this.texture = t;
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
	public void onEntityHit(IEntity entity, Side s) {
		
	}

	@Override
	public void onEntityStep(IEntity entity) {
		
	}

	@Override
	public void onPlayerInteract(Keybind keybind) {
		
	}

}
