package ai.arcblroth.boss.engine.tile;

import ai.arcblroth.boss.engine.IInteractable;
import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.TilePosition;
import ai.arcblroth.boss.render.Texture;

public interface ITile extends IInteractable {
	
	public Room getRoom();
	
	public TilePosition getPosition();

	public String getId();
	
	public Texture getTexture();
	
	public boolean isPassable();
	
	public double getViscosity();
	
}
