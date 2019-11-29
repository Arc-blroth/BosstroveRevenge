package ai.arcblroth.boss.engine.tile;

import ai.arcblroth.boss.register.IRegistrable;
import ai.arcblroth.boss.render.Texture;

public interface ITile extends IRegistrable<ITile> {
	
	public enum Side {NORTH, EAST, SOUTH, WEST};
	
	public Texture getTexture();
	
	public boolean isPassable();
	
	public double getViscosity();
	
	public void onPlayerHit(Side s);
	
	public void onPlayerStep();
	
}
