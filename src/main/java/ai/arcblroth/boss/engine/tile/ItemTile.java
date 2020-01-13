package ai.arcblroth.boss.engine.tile;

import ai.arcblroth.boss.engine.IInteractable.Direction;
import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.engine.item.IItem;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.render.Texture;

public class ItemTile implements ITile {
	
	private IItem item;

	public ItemTile(IItem item) {
		this.item = item;
	}
	
	@Override
	public Texture getTexture() {
		return item.getTexture();
	}

	@Override
	public final boolean isPassable() {
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
