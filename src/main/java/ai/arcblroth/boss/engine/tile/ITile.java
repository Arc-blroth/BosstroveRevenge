package ai.arcblroth.boss.engine.tile;

import ai.arcblroth.boss.engine.IInteractable;
import ai.arcblroth.boss.register.IRegistrable;
import ai.arcblroth.boss.render.Texture;

public interface ITile extends IInteractable {
	
	public Texture getTexture();
	
	public boolean isPassable();
	
	public double getViscosity();
	
}
