package ai.arcblroth.boss.resource.load;

import ai.arcblroth.boss.engine.Position;
import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.TilePosition;
import ai.arcblroth.boss.engine.hitbox.Hitbox;
import ai.arcblroth.boss.game.RoomEngine;
import ai.arcblroth.boss.game.WorldEngine;
import ai.arcblroth.boss.register.AreaRegistry;
import ai.arcblroth.boss.register.EntityRegistry;
import ai.arcblroth.boss.register.FloorTileRegistry;
import ai.arcblroth.boss.register.WallTileRegistry;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.resource.load.exception.MalformedSpecificationException;
import ai.arcblroth.boss.util.Pair;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoomLoader {

	private static final Logger logger = Logger.getLogger("RoomLoader");
	
	public static Map<String, Room> loadRooms(JsonArray roomArray, JsonArray paletteArray) {
		HashMap<String, Room> rooms = new HashMap<>();
		roomArray.forEach((roomObj) -> {
			try {
				Pair<String, Room> idAndRoom = loadRoom(roomObj, paletteArray);
				if(rooms.containsKey(idAndRoom.getFirst())) {
					throw new IllegalStateException("More than one room is defined with id \"" + idAndRoom.getFirst() + "\n");
				}
				rooms.put(idAndRoom.getFirst(), idAndRoom.getSecond());
			} catch(Exception e) {
				logger.log(Level.WARNING, "Could not load Room", e);
			}
		});
		return rooms;
	}
	
	public static Pair<String, Room> loadRoom(JsonElement roomEle, JsonArray paletteArray) throws MalformedSpecificationException {
		if(!roomEle.isJsonObject()) throw new MalformedSpecificationException("rooms");
		JsonObject roomObj = roomEle.getAsJsonObject();
		try {
			String roomId = roomObj.get("roomId").getAsString();
			int width = roomObj.get("width").getAsInt();
			int height = roomObj.get("height").getAsInt();
			Position initialPos = new Position(0, 0);
			if(roomObj.has("initialX") && roomObj.has("initialY")) {
				initialPos = new Position(
						roomObj.get("initialX").getAsDouble(),
						roomObj.get("initialY").getAsDouble()
				);
			}
			Color resetColor = Color.BLACK;
			if(roomObj.has("resetColor")) {
				resetColor = new Color(Integer.parseUnsignedInt("ff" + roomObj.get("resetColor").getAsString().replace("#", ""), 16));
			}
			Function<WorldEngine, RoomEngine> roomEngineBuilder;
			if(roomObj.has("roomEngine")) {
				String roomEngineName = roomObj.get("roomEngine").getAsString();
				try {
					Class<? extends RoomEngine> roomEngine = (Class<? extends RoomEngine>) Class.forName(roomEngineName);
					Constructor<? extends RoomEngine> roomEngineConstructor = roomEngine.getConstructor(WorldEngine.class);
					roomEngineBuilder = worldEngine -> {
						try {
							return roomEngineConstructor.newInstance(worldEngine);
						} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
							logger.log(Level.SEVERE, String.format("Could not construct roomEngine \"%s\" in room \"%s\": ", roomEngineName, roomId), e);
							return null;
						}
					};
				} catch (ClassNotFoundException | NoSuchMethodException e) {
					logger.log(Level.WARNING, String.format("Could not load roomEngine \"%s\" in room \"%s\": ", roomEngineName, roomId), e);
					roomEngineBuilder = worldEngine -> null;
				}
			} else {
				roomEngineBuilder = worldEngine -> null;
			}

			Room outRoom = new Room(width, height, initialPos, resetColor, roomEngineBuilder);
			
			{
				JsonArray floorTiles = roomObj.get("floorTiles").getAsJsonArray();
				if(floorTiles.size() != height) throw new IllegalArgumentException("floorTiles.length != height");
				for(int y = 0; y < floorTiles.size(); y++) {
					JsonArray floorTileRow = floorTiles.get(y).getAsJsonArray();
					if(floorTileRow.size() != width) throw new IllegalArgumentException("floorTileRow.length != width");
					for(int x = 0; x < floorTileRow.size(); x++) {
						JsonElement floorTile = floorTileRow.get(x);
						if(floorTile.isJsonPrimitive() && floorTile.getAsJsonPrimitive().isNumber()) {
							int paletteIndex = floorTile.getAsJsonPrimitive().getAsInt();
							if(paletteIndex >= paletteArray.size()) {
								logger.log(Level.WARNING,
										String.format("Could not find floorTile at (%s, %s) in room \"%s\": palette index \"%s\" out of bounds", x, y, roomId, paletteIndex));
								continue;
							} else {
								floorTile = paletteArray.get(paletteIndex);
							}
						}
						if(floorTile.isJsonObject()) {
							try {
								
								String floorTileName = floorTile.getAsJsonObject().get("tileId").getAsString();
								if(FloorTileRegistry.instance().containsKey(floorTileName)) {
									outRoom.getFloorTiles().set(x, y, 
											FloorTileRegistry.instance().buildTile(floorTileName, outRoom, new TilePosition(x, y), floorTile.getAsJsonObject())
									);
								} else {
									logger.log(Level.WARNING, 
											String.format("Could not find floorTile \"%s\" at (%s, %s) in room \"%s\"", floorTileName, x, y, roomId));
								}
								
							} catch(Exception e) {
								logger.log(Level.WARNING, 
										String.format("Could not load floorTile object at (%s, %s) in room \"%s\": ", x, y, roomId), e);
							}
						} else if(floorTile.isJsonNull()) {
							//Ignore null tiles
						} else {
							String floorTileName = floorTile.getAsString();
							if(FloorTileRegistry.instance().containsKey(floorTileName)) {
								outRoom.getFloorTiles().set(x, y, 
										FloorTileRegistry.instance().buildTile(floorTileName, outRoom, new TilePosition(x, y), new JsonObject())
								);
							} else {
								logger.log(Level.WARNING, 
										String.format("Could not find floorTile \"%s\" at (%s, %s) in room \"%s\"", floorTileName, x, y, roomId));
							}
						}
					}
				}
			}
			
			{
				JsonArray wallTiles = roomObj.get("wallTiles").getAsJsonArray();
				if(wallTiles.size() != height) throw new IllegalArgumentException("wallTiles.length != height");
				for(int y = 0; y < wallTiles.size(); y++) {
					JsonArray wallTileRow = wallTiles.get(y).getAsJsonArray();
					if(wallTileRow.size() != width) throw new IllegalArgumentException("wallTileRow.length != width");
					for(int x = 0; x < wallTileRow.size(); x++) {
						JsonElement wallTile = wallTileRow.get(x);
						if(wallTile.isJsonPrimitive() && wallTile.getAsJsonPrimitive().isNumber()) {
							int paletteIndex = wallTile.getAsJsonPrimitive().getAsInt();
							if(paletteIndex >= paletteArray.size()) {
								logger.log(Level.WARNING,
										String.format("Could not find wallTile at (%s, %s) in room \"%s\": palette index \"%s\" out of bounds", x, y, roomId, paletteIndex));
								continue;
							} else {
								wallTile = paletteArray.get(paletteIndex);
							}
						}
						if(wallTile.isJsonObject()) {
							try {
								String wallTileName = wallTile.getAsJsonObject().get("tileId").getAsString();
								if(WallTileRegistry.instance().containsKey(wallTileName)) {
									outRoom.getWallTiles().set(x, y, 
											WallTileRegistry.instance().buildTile(wallTileName, outRoom, new TilePosition(x, y), wallTile.getAsJsonObject())
									);
								} else {
									logger.log(Level.WARNING, 
											String.format("Could not find wallTile \"%s\" at (%s, %s) in room \"%s\"", wallTileName, x, y, roomId));
								}
								
							} catch(Exception e) {
								logger.log(Level.WARNING, 
										String.format("Could not load wallTile object at (%s, %s) in room \"%s\": ", x, y, roomId), e);
							}
						} else if(wallTile.isJsonNull()) {
							//Ignore null tiles
						} else {
							String wallTileName = wallTile.getAsString();
							if(WallTileRegistry.instance().containsKey(wallTileName)) {
								try {
									outRoom.getWallTiles().set(x, y, 
											WallTileRegistry.instance().buildTile(wallTileName, outRoom, new TilePosition(x, y), new JsonObject())
									);
								} catch(Exception e) {
									logger.log(Level.WARNING, 
											String.format("Could not load wallTile object at (%s, %s) in room \"%s\": ", x, y, roomId), e);
								}
							} else {
								logger.log(Level.WARNING, 
										String.format("Could not find wallTile \"%s\" at (%s, %s) in room \"%s\"", wallTileName, x, y, roomId));
							}
						}
					}
				}
			}
			
			{
				if(roomObj.has("entities")) {
					JsonArray entities = roomObj.get("entities").getAsJsonArray();
					entities.forEach((entEle) -> {
						try {
							JsonObject ent = entEle.getAsJsonObject();
							String entityId = ent.get("entityId").getAsString();
							Double xPos = ent.get("x").getAsDouble();
							Double yPos = ent.get("y").getAsDouble();
							if(EntityRegistry.instance().containsKey(entityId)) {
								outRoom.getEntities().add(EntityRegistry.instance().buildEntity(entityId, outRoom, new Position(xPos, yPos), ent));
							} else {
								logger.log(Level.WARNING, 
										String.format("No entity with entityId \"%s\" is registered, in room \"%s\"", entityId, roomId));
							}
						} catch(Exception e) {
							logger.log(Level.WARNING, 
									String.format("Could not load entity in room \"%s\": ", roomId), e);
						}
					});
				}
			}

			{
				if(roomObj.has("areas")) {
					JsonArray areas = roomObj.get("areas").getAsJsonArray();
					areas.forEach((areaEle) -> {
						try {
							JsonObject areaObj = areaEle.getAsJsonObject();
							String areaId = areaObj.get("areaId").getAsString();
							JsonObject hitboxObj = areaObj.get("hitbox").getAsJsonObject();
							Hitbox hitbox = new Hitbox(hitboxObj.get("x").getAsDouble(), hitboxObj.get("y").getAsDouble(), hitboxObj.get("w").getAsDouble(), hitboxObj.get("h").getAsDouble());
							if(AreaRegistry.instance().containsKey(areaId)) {
								outRoom.getAreas().add(AreaRegistry.instance().buildArea(areaId, outRoom, hitbox, areaObj));
							} else {
								logger.log(Level.WARNING,
										String.format("No area with areaId \"%s\" is registered, in room \"%s\"", areaId, roomId));
							}
						} catch(Exception e) {
							logger.log(Level.WARNING,
									String.format("Could not load area in room \"%s\": ", roomId), e);
						}
					});
				}
			}
			
			return new Pair<String, Room>(roomId, outRoom);
			
		} catch (IllegalArgumentException e) {
			throw new MalformedSpecificationException(e);
		} catch (Exception e) {
			MalformedSpecificationException mse = new MalformedSpecificationException("rooms");
			mse.initCause(e);
			throw mse;
		}
	}

}
