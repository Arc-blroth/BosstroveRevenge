package ai.arcblroth.boss.game;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.engine.IEngine;
import ai.arcblroth.boss.engine.IInteractable.Direction;
import ai.arcblroth.boss.engine.Position;
import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.StepEvent;
import ai.arcblroth.boss.engine.entity.Xulpir;
import ai.arcblroth.boss.engine.entity.player.Player;
import ai.arcblroth.boss.event.SubscribeEvent;
import ai.arcblroth.boss.key.CharacterInputEvent;
import ai.arcblroth.boss.register.FloorTileRegistry;
import ai.arcblroth.boss.register.LevelRegistry;
import ai.arcblroth.boss.register.WallTileRegistry;
import ai.arcblroth.boss.render.IRenderer;
import ai.arcblroth.boss.util.StaticDefaults;

public class WorldEngine implements IEngine {
	
	private WorldRenderer renderer;
	private Room room;

	public WorldEngine() {
		this.room = LevelRegistry.get().get("w0l1").getRoom("0");
		this.renderer = new WorldRenderer(room);
		room.getEntities().add(new Xulpir(new Position(5, 5), 10));
	}
	
	@Override
	@SubscribeEvent
	public void step(StepEvent e) {
		room.runCollisionCallbacks();
		
		Player player = room.getPlayer();
		renderer.setRenderOffset(
				player.getPosition().getX() * StaticDefaults.TILE_WIDTH - StaticDefaults.OUTPUT_WIDTH / 2D,
				player.getPosition().getY() * StaticDefaults.TILE_HEIGHT - StaticDefaults.OUTPUT_HEIGHT / 2D
		);
	}

	@Override
	@SubscribeEvent
	public void handleKeyInput(CharacterInputEvent e) {
		Player player = room.getPlayer();
		if(e.getKey() == 'w') {
			player.setDirection(Direction.NORTH);
			player.accelerate(Direction.NORTH, 0.25);
		} else if(e.getKey() == 'd') {
			player.setDirection(Direction.EAST);
			player.accelerate(Direction.EAST, 0.25);
		} else if(e.getKey() == 'a') {
			player.setDirection(Direction.WEST);
			player.accelerate(Direction.WEST, 0.25);
		} else if(e.getKey() == 's') {
			player.setDirection(Direction.SOUTH);
			player.accelerate(Direction.SOUTH, 0.25);
		}
	}

	@Override
	public IRenderer getRenderer() {
		return renderer;
	}

}
