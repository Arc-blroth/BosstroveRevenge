package ai.arcblroth.boss.engine;

import java.util.ArrayList;

import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.engine.tile.FloorTile;
import ai.arcblroth.boss.engine.tile.WallTile;
import ai.arcblroth.boss.register.FloorTileRegistry;
import ai.arcblroth.boss.register.WallTileRegistry;
import ai.arcblroth.boss.util.Grid2D;

public class Room {
	
	private Grid2D<FloorTile> floorTiles;
	private Grid2D<WallTile> wallTiles;
	private ArrayList<IEntity> entities;
	private int width, height;
	
	public Room(int width, int height) {
		if(width < 1 || height < 1) throw new IllegalArgumentException("Room width and height must be >1");
		
		this.width = width;
		this.height = height;
		this.floorTiles = new Grid2D<FloorTile>(width, height, FloorTileRegistry.getTile("empty"));
		this.wallTiles = new Grid2D<WallTile>(width, height, WallTileRegistry.getTile("empty"));
		this.entities = new ArrayList<>();
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

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	
}
