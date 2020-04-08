package ai.arcblroth.boss.engine;

import ai.arcblroth.boss.engine.ast.Variable;
import ai.arcblroth.boss.engine.entity.player.Player;
import ai.arcblroth.boss.game.WorldEngine;
import ai.arcblroth.boss.register.IRegistrable;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.util.StaticDefaults;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Level implements IRegistrable<Level> {

	public static final String GLOBAL_DATA_PREFIX = "#";
	public static final String PERSISTENT_DATA_PREFIX = "%";
	public static final String TRIGGER_DATA_PREFIX = "$";

	private String world;
	private String level;
	private String title;
	private Color introBackgroundColor;
	private Color introForegroundColor;
	
	private Map<String, Variable> globalData;
	private Map<String, Variable> persistentData;
	private Map<String, Variable> triggerData;
	
	private Map<String, Room> rooms;

	private String initalRoom;

	private Player player;
	
	public Level(String world, String level, String title, Color introBackgroundColor, Color introForegroundColor, WorldEngine engine) {
		this.world = world;
		this.level = level;
		this.title = title;
		this.introBackgroundColor = introBackgroundColor;
		this.introForegroundColor = introForegroundColor;
		this.globalData = new TreeMap<>();
		this.persistentData = new TreeMap<>();
		this.triggerData = new TreeMap<>();
		this.rooms = new HashMap<>();
		this.player = new Player(new Position(0, 0), StaticDefaults.MAX_PLAYER_HEALTH);
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

	public Color getIntroBackgroundColor() {
		return introBackgroundColor;
	}

	public Color getIntroForegroundColor() {
		return introForegroundColor;
	}

	public Room getRoom(String key) {
		return rooms.get(key);
	}

	public boolean hasRoom(String key) {
		return rooms.containsKey(key);
	}

	public void setInitialRoom(String id) {
		if(!rooms.containsKey(id)) throw new IllegalArgumentException("initalRoom id is not a valid room id");
		this.initalRoom = id;
	}

	public String getInitialRoom() {
		return initalRoom;
	}

	public Player getPlayer() {
		return player;
	}
}
