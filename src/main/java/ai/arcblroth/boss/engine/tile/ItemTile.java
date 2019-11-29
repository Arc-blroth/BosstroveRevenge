package ai.arcblroth.boss.engine.tile;

import ai.arcblroth.boss.engine.item.IItem;
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
	public void onPlayerHit(Side s) {
		
	}

	@Override
	public void onPlayerStep() {
		
	}

}
