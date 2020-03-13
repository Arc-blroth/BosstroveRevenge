package ai.arcblroth.boss.game.tile;

import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.TilePosition;
import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.engine.entity.player.Player;
import ai.arcblroth.boss.engine.tile.WallTile;
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
	public void onEntityStep(IEntity entity) {
		if(entity instanceof Player) {
			activated = true;
		}
	}
	
	@Override
	public Texture getTexture() {
		if(activated) {
			currentAnimationFrame = Math.min(currentAnimationFrame + 0.1, texture.getFrames() - 1);
		} else {
			currentAnimationFrame = Math.max(currentAnimationFrame - 0.1, 0);
		}
		System.out.println(currentAnimationFrame);
		texture.setCurrentFrame((int)Math.round(currentAnimationFrame));
		return texture;
	}
	
	@Override
	public boolean isPassable() {
		return true;
	}

}
