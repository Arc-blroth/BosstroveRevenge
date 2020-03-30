package ai.arcblroth.boss.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import ai.arcblroth.boss.engine.ast.Variable;
import ai.arcblroth.boss.engine.gui.GUI;
import ai.arcblroth.boss.game.WorldEngine;
import ai.arcblroth.boss.register.IRegistrable;

public class Level implements IRegistrable<Level> {

	public static final String GLOBAL_DATA_PREFIX = "#";
	public static final String PERSISTENT_DATA_PREFIX = "%";
	public static final String TRIGGER_DATA_PREFIX = "$";

	private String world;
	private String level;
	private String title;
	
	private Map<String, Variable> globalData;
	private Map<String, Variable> persistentData;
	private Map<String, Variable> triggerData;
	
	private Map<String, Room> rooms;

	private String initalRoom;
	
	public Level(String world, String level, String title, WorldEngine engine) {
		this.world = world;
		this.level = level;
		this.title = title;
		this.globalData = new TreeMap<>();
		this.persistentData = new TreeMap<>();
		this.triggerData = new TreeMap<>();
		this.rooms = new HashMap<>();
	}
	
	public void addRoom(String id, Room room) {
		room._setLevel(this);
		rooms.put(id, room);
	}

	public String getWorldString() {
		return world;
	}

	public String getLevelString() {
		return level;
	}

	public String getTitleString() {
		return title;
	}

	public Room getRoom(String key) {
		return rooms.get(key);
	}

	public void setInitialRoom(String id) {
		if(!rooms.containsKey(id)) throw new IllegalArgumentException("initalRoom id is not a valid room id");
		this.initalRoom = id;
	}

	public String getInitialRoom() {
		return initalRoom;
	}

}
