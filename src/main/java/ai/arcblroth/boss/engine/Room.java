package ai.arcblroth.boss.engine;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.JsonObject;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.engine.entity.IAccelerable;
import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.engine.entity.player.Player;
import ai.arcblroth.boss.engine.hitbox.Hitbox;
import ai.arcblroth.boss.engine.hitbox.HitboxManager;
import ai.arcblroth.boss.engine.tile.FloorTile;
import ai.arcblroth.boss.engine.tile.ITile;
import ai.arcblroth.boss.engine.tile.WallTile;
import ai.arcblroth.boss.register.FloorTileRegistry;
import ai.arcblroth.boss.register.WallTileRegistry;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.util.Grid2D;
import ai.arcblroth.boss.util.StaticDefaults;
import ai.arcblroth.boss.util.Vector2D;

public class Room {

	private Level level;
	private Grid2D<FloorTile> floorTiles;
	private Grid2D<WallTile> wallTiles;
	private ArrayList<IEntity> entities;
	private HitboxManager hitboxManager;
	private Player player;
	private int width, height;
	private Color resetColor;

	public Room(Level level, int width, int height, Position initPlayerPosition, Color resetColor) {
		if(width < 1 || height < 1) throw new IllegalArgumentException("Room width and height must be >1");
		
		this.level = level;
		this.width = width;
		this.height = height;
		this.resetColor = resetColor;
		
		this.floorTiles = new Grid2D<FloorTile>(width, height, null);
		this.wallTiles = new Grid2D<WallTile>(width, height, null);
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				floorTiles.set(x, y, FloorTileRegistry.instance().buildTile("empty", this, new TilePosition(x, y)));
				wallTiles.set(x, y, WallTileRegistry.instance().buildTile("empty", this, new TilePosition(x, y)));
			}
		}
		
		this.entities = new ArrayList<>();
		this.player = new Player(initPlayerPosition, StaticDefaults.MAX_PLAYER_HEALTH);
		this.hitboxManager = new HitboxManager(width, height);
	}
	
	public Room(Level level, int width, int height, Position initPlayerPosition) {
		this(level, width, height, initPlayerPosition, Color.BLACK);
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
			
			if(entity instanceof IAccelerable) {
				moveAccelerableEntity(entity);
			}
			
			hitboxManager.getAllCollisionsOf(entity).forEach((IHitboxed other) -> {
				if(other instanceof IEntity) {
					entity.onEntityStep((IEntity) other);
					// We don't call onEntityStep on the other entity
					// to prevent duplicate calls.
				}
			});
		}

		moveAccelerableEntity(player);
		
	}
	
	private <E extends IEntity & IAccelerable> void moveAccelerableEntity(IEntity accelerableEntity) {
		
		if(!(accelerableEntity instanceof IAccelerable)) return;
		
		@SuppressWarnings("unchecked")
		E ent = (E) accelerableEntity;
		
		AtomicBoolean isGoingToCrashX = new AtomicBoolean(false);
		AtomicBoolean isGoingToCrashY = new AtomicBoolean(false);
		
		ent.setAccelerationVector(ent.getAccelerationVector().multiply(ent.getFrictionFactor()));
		
		Vector2D steppedAccel = ent.getAccelerationVector().multiply(1D / 16D);
		
		collisionSteps:
		for(int collisionSubdivisions = 0; collisionSubdivisions < 16; collisionSubdivisions++) {
			
			if(!isGoingToCrashX.get()) {
				ent.setPosition(new Position(
						ent.getPosition().getX() + steppedAccel.getX(),
						ent.getPosition().getY()
				));
				
				hitboxManager.getAllCollisionsOf(ent).forEach((IHitboxed other) -> {
					if(other instanceof TileHitboxWrapper) {
						isGoingToCrashX.set(true);
						//ent.setAccelerationVector(
						//		ent.getAccelerationVector().multiply(((TileHitboxWrapper) other).getTile().getViscosity()
						//));
					}
				});
				
				if(isGoingToCrashX.get()) {
					ent.setPosition(new Position(
							ent.getPosition().getX() - steppedAccel.getX(),
							ent.getPosition().getY()
					));
					ent.setAccelerationVector(new Vector2D(0D, ent.getAccelerationVector().getY()));
				}
			}
			
			if(!isGoingToCrashY.get()) {
				ent.setPosition(new Position(
						ent.getPosition().getX(),
						ent.getPosition().getY() + steppedAccel.getY()
				));
				
				hitboxManager.getAllCollisionsOf(ent).forEach((IHitboxed other) -> {
					if(other instanceof TileHitboxWrapper) {
						isGoingToCrashY.set(true);
					}
				});
				
				if(isGoingToCrashY.get()) {
					ent.setPosition(new Position(
							ent.getPosition().getX(),
							ent.getPosition().getY() - steppedAccel.getY()
					));
					ent.setAccelerationVector(new Vector2D(ent.getAccelerationVector().getX(), 0D));
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
	
	public Color getResetColor() {
		return resetColor;
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