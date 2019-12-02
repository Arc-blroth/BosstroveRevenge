package ai.arcblroth.boss.engine;

import java.util.ArrayList;

import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.engine.tile.FloorTile;
import ai.arcblroth.boss.engine.tile.WallTile;
import ai.arcblroth.boss.register.FloorTileRegistry;

public class Room {
	
	private ArrayList<ArrayList<FloorTile>> floorTiles;
	private ArrayList<ArrayList<WallTile>> wallTiles;
	private ArrayList<? extends IEntity> entities;
	private int width, height;
	
	public Room(int width, int height) {
		if(width < 1 || height < 1) throw new IllegalArgumentException("Room width and height must be >1");
		
		this.width = width;
		this.height = height;
		this.floorTiles = new ArrayList<>(height);
		this.wallTiles = new ArrayList<>(height);
		this.entities = new ArrayList<>();
		
		for(int y = 0; y < height; y++) {
			floorTiles.add(new ArrayList<>(width));
			for(int x = 0; x < width; x++) {
				floorTiles.get(y).add(FloorTileRegistry.getTile("empty"));
			}
		}
		
	}
	
}
