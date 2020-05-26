package ai.arcblroth.boss.game.tile;

import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.TilePosition;
import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.engine.entity.player.Player;
import ai.arcblroth.boss.engine.tile.WallTile;
import ai.arcblroth.boss.register.WallTileBuilder;
import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.util.StaticDefaults;
import com.google.gson.JsonObject;

public class RoomChangeTile extends WallTile {

	public final String targetRoom;

	public RoomChangeTile(Room room, TilePosition pos, Texture texture, String targetRoom) {
		super(room, pos, texture);
		this.targetRoom = targetRoom;
	}

	@Override
	public void onEntityStep(IEntity entity) {
		if(entity instanceof Player) {
			getRoom().getLevel().getEngine().setCurrentRoomId(targetRoom);
		}
	}

	@Override
	public String getId() {
		return "boss.roomChange";
	}

	@Override
	public boolean isPassable() {
		return true;
	}

	public static final class Builder extends WallTileBuilder<RoomChangeTile> {

		public Builder(Texture texture) {
			super(texture);
		}

		@Override
		public RoomChangeTile build(Room room, TilePosition tilePos, JsonObject context) {
			if(context.has("target")) {
				String target = context.get("target").getAsString();
				return new RoomChangeTile(room, tilePos, StaticDefaults.EMPTY_TEXTURE, target);
			} else {
				throw new IllegalArgumentException("Must specify a target room for roomChange tile!");
			}
		}

	}

}
