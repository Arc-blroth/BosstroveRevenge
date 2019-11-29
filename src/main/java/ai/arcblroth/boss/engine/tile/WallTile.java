package ai.arcblroth.boss.engine.tile;

import ai.arcblroth.boss.render.Texture;

public class WallTile implements ITile {

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
	public void onPlayerHit(Side s) {
		
	}

	@Override
	public void onPlayerStep() {
		
	}

}
