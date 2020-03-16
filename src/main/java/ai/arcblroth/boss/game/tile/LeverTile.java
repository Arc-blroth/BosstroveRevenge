package ai.arcblroth.boss.game.tile;

import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.TilePosition;
import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.engine.entity.player.Player;
import ai.arcblroth.boss.engine.tile.WallTile;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.render.MultiFrameTexture;
import ai.arcblroth.boss.render.Texture;

public class LeverTile extends WallTile {

	private MultiFrameTexture texture;
	private volatile boolean activated;
	private double currentAnimationFrame = 0;
	
	public LeverTile(Room room, TilePosition pos, MultiFrameTexture t, boolean activated) {
		super(room, pos, t);
		this.activated = activated;
		this.texture = t;
	}
	
	@Override
	public void onStep() {
		if(activated) {
			currentAnimationFrame = Math.min(currentAnimationFrame + 0.5, texture.getFrames() - 1);
		} else {
			currentAnimationFrame = Math.max(currentAnimationFrame - 0.5, 0);
		}
		texture.setCurrentFrame((int)Math.round(currentAnimationFrame));
	}

	@Override
	public void onEntityStep(IEntity entity) {
		
	}
	
	@Override
	public void onPlayerInteract(Keybind k) {
		if(k.getKeybindId().equals("boss.use")) {
			activated = true;
		}
	}
	
	@Override
	public Texture getTexture() {
		return texture;
	}
	
	@Override
	public boolean isPassable() {
		return true;
	}

}
