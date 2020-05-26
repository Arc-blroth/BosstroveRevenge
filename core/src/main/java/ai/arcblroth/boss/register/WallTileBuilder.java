package ai.arcblroth.boss.register;

import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.TilePosition;
import ai.arcblroth.boss.engine.hitbox.Hitbox;
import ai.arcblroth.boss.engine.tile.WallTile;
import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.util.TriFunction;
import com.google.gson.JsonObject;

public abstract class WallTileBuilder<T extends WallTile> implements TriFunction<Room, TilePosition, JsonObject, T> {
	
	private final Texture tileTexture;
	private final Hitbox tileHitbox;
	
	public WallTileBuilder(Texture texture, Hitbox hitbox) {
		this.tileTexture = texture;
		this.tileHitbox = hitbox;
	}
	
	public abstract T build(Room room, TilePosition tilePos, JsonObject context);
	
	public final T apply(Room room, TilePosition tilePos, JsonObject context) {
		return build(room, tilePos, context);
	}
	
	public Texture getTileTexture() {
		return tileTexture;
	}

	public Hitbox getTileHitbox() {
		return tileHitbox;
	}
}
