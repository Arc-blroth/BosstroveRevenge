package ai.arcblroth.boss.resource.load;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ai.arcblroth.boss.engine.Position;
import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.resource.load.exception.MalformedSpecificationException;
import ai.arcblroth.boss.util.Pair;

public class RoomLoader {

	private static final Logger logger = Logger.getLogger("RoomLoader");
	
	public static HashMap<String, Room> loadRooms(JsonArray roomArray) {
		HashMap<String, Room> rooms = new HashMap<>();
		roomArray.forEach((roomObj) -> {
			try {
				Pair<String, Room> idAndRoom = loadRoom(roomObj);
				rooms.put(idAndRoom.getFirst(), idAndRoom.getSecond());
			} catch(Exception e) {
				logger.log(Level.INFO, "Could not load Room", e);
			}
		});
		return rooms;
	}
	
	public static Pair<String, Room> loadRoom(JsonElement roomEle) throws MalformedSpecificationException {
		if(!roomEle.isJsonObject()) throw new MalformedSpecificationException("rooms");
		JsonObject roomObj = roomEle.getAsJsonObject();
		try {
			String roomId = roomObj.get("roomId").getAsString();
			int width = roomObj.get("width").getAsInt();
			int height = roomObj.get("height").getAsInt();
			boolean initial = roomObj.get("initial").getAsBoolean();
			Position initialPos = new Position(0, 0);
			if(initial) {
				initialPos = new Position(
						roomObj.get("initialX").getAsDouble(),
						roomObj.get("initialY").getAsDouble()
				);
			}
			Room outRoom = new Room(width, height, initialPos);
			
			
			
			return new Pair<String, Room>(roomId, outRoom);
		} catch (Exception e) {
			throw new MalformedSpecificationException("rooms");
		}
	}
}
