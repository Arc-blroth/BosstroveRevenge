package ai.arcblroth.boss.engine;

import java.util.ArrayList;

import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.engine.entity.player.Player;
import ai.arcblroth.boss.engine.tile.FloorTile;
import ai.arcblroth.boss.engine.tile.WallTile;
import ai.arcblroth.boss.register.FloorTileRegistry;
import ai.arcblroth.boss.register.WallTileRegistry;
import ai.arcblroth.boss.util.Grid2D;
import ai.arcblroth.boss.util.StaticDefaults;

public class Room {
	
	private Grid2D<FloorTile> floorTiles;
	private Grid2D<WallTile> wallTiles;
	private ArrayList<IEntity> entities;
	private Player player;
	private int width, height;
	
	public Room(int width, int height, Position initPlayerPosition) {
		if(width < 1 || height < 1) throw new IllegalArgumentException("Room width and height must be >1");
		
		this.width = width;
		this.height = height;
		this.floorTiles = new Grid2D<FloorTile>(width, height, FloorTileRegistry.get().getTile("empty"));
		this.wallTiles = new Grid2D<WallTile>(width, height, WallTileRegistry.get().getTile("empty"));
		this.entities = new ArrayList<>();
		this.player = new Player(initPlayerPosition, StaticDefaults.MAX_PLAYER_HEALTH);
		entities.add(player);
	}

	public ArrayList<IEntity> getEntities() {
		return entities;
	}

	public Grid2D<FloorTile> getFloorTiles() {
		return floorTiles;
	}

	public Grid2D<WallTile> getWallTiles() {
		return wallTiles;
	}
	
	public Player getPlayer() {
		return player;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	
}
