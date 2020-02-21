package ai.arcblroth.boss.game;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.engine.IEngine;
import ai.arcblroth.boss.engine.IInteractable.Direction;
import ai.arcblroth.boss.engine.Level;
import ai.arcblroth.boss.engine.StepEvent;
import ai.arcblroth.boss.engine.entity.player.Player;
import ai.arcblroth.boss.key.CharacterInputEvent;
import ai.arcblroth.boss.register.LevelRegistry;
import ai.arcblroth.boss.render.IRenderer;
import ai.arcblroth.boss.util.Pair;
import ai.arcblroth.boss.util.StaticDefaults;

public class WorldEngine implements IEngine {
	
	private WorldRenderer renderer;
	private Level level;
	private String currentRoom;

	public WorldEngine() {
		this.level = LevelRegistry.instance().get("w0l1");
		currentRoom = "0";
		this.renderer = new WorldRenderer(level.getRoom(currentRoom));
		BosstrovesRevenge.instance().setResetColor(level.getRoom(currentRoom).getResetColor());
	}
	
	@Override
	public void step(StepEvent e) {
		level.getRoom(currentRoom).runCollisionCallbacks();
		
		Player player = level.getRoom(currentRoom).getPlayer();
		Pair<Integer, Integer> outputSize = BosstrovesRevenge.instance().getOutputSize();
		renderer.setRenderOffset(
				player.getPosition().getX() * StaticDefaults.TILE_WIDTH - outputSize.getFirst() / 2D,
				player.getPosition().getY() * StaticDefaults.TILE_HEIGHT - outputSize.getSecond() / 2D
		);
	}

	@Override
	public void handleKeyInput(CharacterInputEvent e) {
		Player player = level.getRoom(currentRoom).getPlayer();
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
