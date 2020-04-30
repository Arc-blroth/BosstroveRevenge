package ai.arcblroth.boss.game.area;

import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.area.Area;
import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.engine.entity.player.Player;
import ai.arcblroth.boss.engine.hitbox.Hitbox;
import com.google.gson.JsonObject;

public class RoomChangeArea extends Area {

	private final String targetRoom;

	public RoomChangeArea(Room room, Hitbox hitbox, JsonObject context) {
		super(room, hitbox, context);
		this.targetRoom = context.get("target").getAsString();
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

}
