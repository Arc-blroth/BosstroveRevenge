package ai.arcblroth.boss.engine.tile;

import ai.arcblroth.boss.render.Texture;

public class FloorTile implements ITile {

	private Texture texture;

	public FloorTile(Texture t) {
		this.texture = t;
	}

	@Override
	public Texture getTexture() {
		return texture;
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
