package ai.arcblroth.boss.register;

import com.google.gson.JsonObject;

import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.TilePosition;
import ai.arcblroth.boss.engine.tile.WallTile;
import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.util.StaticDefaults;
import ai.arcblroth.boss.util.TriFunction;

public abstract class WallTileBuilder<T extends WallTile> implements TriFunction<Room, TilePosition, JsonObject, T> {
	
	private final Texture tileTexture;
	
	public WallTileBuilder(Texture texture) {
		this.tileTexture = texture;
	}
	
	public abstract T build(Room room, TilePosition tilePos, JsonObject context);
	
	public final T apply(Room room, TilePosition tilePos, JsonObject context) {
		return build(room, tilePos, context);
	}
	
	public Texture getTileTexture() {
		return tileTexture;
	}
	
}
