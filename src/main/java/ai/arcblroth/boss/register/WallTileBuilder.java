package ai.arcblroth.boss.register;

import com.google.gson.JsonObject;

import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.TilePosition;
import ai.arcblroth.boss.engine.tile.WallTile;
import ai.arcblroth.boss.util.TriFunction;

public abstract class WallTileBuilder implements TriFunction<Room, TilePosition, JsonObject, WallTile> {
	
	public abstract WallTile build(Room room, TilePosition tilePos, JsonObject context);
	
	public final WallTile apply(Room room, TilePosition tilePos, JsonObject context) {
		return build(room, tilePos, context);
	}
	
}
