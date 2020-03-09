package ai.arcblroth.boss.register;

import com.google.gson.JsonObject;

import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.TilePosition;
import ai.arcblroth.boss.engine.tile.FloorTile;
import ai.arcblroth.boss.util.TriFunction;

public abstract class FloorTileBuilder implements TriFunction<Room, TilePosition, JsonObject, FloorTile> {
	
	public abstract FloorTile build(Room room, TilePosition tilePos, JsonObject context);
	
	public final FloorTile apply(Room room, TilePosition tilePos, JsonObject context) {
		return build(room, tilePos, context);
	}
	
}
