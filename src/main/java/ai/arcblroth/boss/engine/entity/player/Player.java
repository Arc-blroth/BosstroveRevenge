package ai.arcblroth.boss.engine.entity.player;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.engine.IHitboxed;
import ai.arcblroth.boss.engine.Position;
import ai.arcblroth.boss.engine.entity.*;
import ai.arcblroth.boss.engine.hitbox.Hitbox;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.register.FloorTileRegistry;
import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.resource.InternalResource;
import ai.arcblroth.boss.util.StaticDefaults;
import ai.arcblroth.boss.util.TextureUtils;

public class Player extends Mob {
	
	private Texture flippedTexture;
	private boolean flipTexture;

	public Player(Position initialPos, double initialHealth) {
		super(BosstrovesRevenge.instance().getTextureCache().get(new InternalResource("data/texture/entity/daniel.png")), initialPos, 0.6, initialHealth);
		this.flippedTexture = new Texture(TextureUtils.flipX(super.getTexture()));
	}
	
	@Override
	public void onEntityHit(IEntity e, Direction s) {
		
	}

	@Override
	public void onEntityStep(IEntity e) {
		
	}

	@Override
	public void onPlayerInteract(Keybind keybind) {
		// "Stop playing with yourself!"
	}

	@Override
	public Texture getTexture() {
		return flipTexture ? flippedTexture : super.getTexture();
	}

	@Override
	public Hitbox getHitbox() {
		return new Hitbox(-0.5, -0.5, 1, 1).resolveRelativeTo(super.getPosition());
	}

	@Override
	public void setDirection(Direction dir) {
		if(dir == Direction.WEST) flipTexture = true;
		else if(dir == Direction.EAST) flipTexture = false;
		super.setDirection(dir);
	}
	
}
