package ai.arcblroth.boss.engine;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.engine.entity.player.Player;
import ai.arcblroth.boss.engine.hitbox.Hitbox;
import ai.arcblroth.boss.engine.hitbox.HitboxManager;
import ai.arcblroth.boss.engine.tile.FloorTile;
import ai.arcblroth.boss.engine.tile.ITile;
import ai.arcblroth.boss.engine.tile.WallTile;
import ai.arcblroth.boss.register.FloorTileRegistry;
import ai.arcblroth.boss.register.WallTileRegistry;
import ai.arcblroth.boss.util.Grid2D;
import ai.arcblroth.boss.util.StaticDefaults;
import ai.arcblroth.boss.util.Vector2D;

public class Room {
	
	private Grid2D<FloorTile> floorTiles;
	private Grid2D<WallTile> wallTiles;
	private ArrayList<IEntity> entities;
	private HitboxManager hitboxManager;
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
		this.hitboxManager = new HitboxManager(width, height);
	}
	
	public void runCollisionCallbacks() {
		hitboxManager.clear();
		wallTiles.forEach((x, y, wallTile) -> {
			if(!wallTile.isPassable()) hitboxManager.add(new TileHitboxWrapper(x, y, wallTile));
		});
		floorTiles.forEach((x, y, floorTile) -> {
			if(!floorTile.isPassable()) hitboxManager.add(new TileHitboxWrapper(x, y, floorTile));
		});
		entities.forEach(hitboxManager::add);
		hitboxManager.add(player);
		
		for(IEntity entity : entities) {
			hitboxManager.getAllCollisionsOf(entity).forEach((IHitboxed other) -> {
				if(other instanceof IEntity) {
					entity.onEntityStep((IEntity) other);
					// We don't call onEntityStep on the other entity
					// to prevent duplicate calls.
				}
			});
		}
		
		AtomicBoolean isGoingToCrashX = new AtomicBoolean(false);
		AtomicBoolean isGoingToCrashY = new AtomicBoolean(false);
		
		player.setAccelerationVector(player.getAccelerationVector().multiply(player.getFrictionFactor()));
		
		Vector2D steppedAccel = player.getAccelerationVector().multiply(1D / 16D);
		
		collisionSteps:
		for(int collisionSubdivisions = 0; collisionSubdivisions < 16; collisionSubdivisions++) {
			
			if(!isGoingToCrashX.get()) {
				player.setPosition(new Position(
						player.getPosition().getX() + steppedAccel.getX(),
						player.getPosition().getY()
				));
				
				hitboxManager.getAllCollisionsOf(player).forEach((IHitboxed other) -> {
					if(other instanceof TileHitboxWrapper) {
						isGoingToCrashX.set(true);
						//player.setAccelerationVector(
						//		player.getAccelerationVector().multiply(((TileHitboxWrapper) other).getTile().getViscosity()
						//));
					}
				});
				
				if(isGoingToCrashX.get()) {
					player.setPosition(new Position(
							player.getPosition().getX() - steppedAccel.getX(),
							player.getPosition().getY()
					));
					player.setAccelerationVector(new Vector2D(0D, player.getAccelerationVector().getY()));
				}
			}
			
			if(!isGoingToCrashY.get()) {
				player.setPosition(new Position(
						player.getPosition().getX(),
						player.getPosition().getY() + steppedAccel.getY()
				));
				
				hitboxManager.getAllCollisionsOf(player).forEach((IHitboxed other) -> {
					if(other instanceof TileHitboxWrapper) {
						isGoingToCrashY.set(true);
					}
				});
				
				if(isGoingToCrashY.get()) {
					player.setPosition(new Position(
							player.getPosition().getX(),
							player.getPosition().getY() - steppedAccel.getY()
					));
					player.setAccelerationVector(new Vector2D(player.getAccelerationVector().getX(), 0D));
				}
			}
			
			if(isGoingToCrashX.get() && isGoingToCrashY.get()) break collisionSteps;
		}
		
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

class TileHitboxWrapper implements IHitboxed {
	
	private static final Hitbox HITBOX = new Hitbox(0, 0, 1, 1);
	
	private int x, y;
	private ITile tile;
	
	TileHitboxWrapper(int x, int y, ITile tile) {
		this.x = x;
		this.y = y;
		this.tile = tile;
	}
	
	@Override
	public Hitbox getHitbox() {
		return HITBOX.resolveRelativeTo(new Position(x, y));
	}
	
	public ITile getTile() {
		return tile;
	}
	
}