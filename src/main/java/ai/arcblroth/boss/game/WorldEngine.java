package ai.arcblroth.boss.game;

import ai.arcblroth.boss.engine.IEngine;
import ai.arcblroth.boss.engine.IInteractable.Direction;
import ai.arcblroth.boss.engine.Position;
import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.StepEvent;
import ai.arcblroth.boss.engine.entity.player.Player;
import ai.arcblroth.boss.event.SubscribeEvent;
import ai.arcblroth.boss.key.CharacterInputEvent;
import ai.arcblroth.boss.register.FloorTileRegistry;
import ai.arcblroth.boss.render.IRenderer;

public class WorldEngine implements IEngine {
	
	private WorldRenderer renderer;
	private Room room;

	public WorldEngine() {
		this.room = new Room(40, 40, new Position(0, 0));
		this.renderer = new WorldRenderer(room);
		room.getFloorTiles().set(0, 0, FloorTileRegistry.get().getTile("boss.sand"));
	}
	
	@Override
	@SubscribeEvent
	public void step(StepEvent e) {
		Position playerPos = room.getPlayer().getPosition();
		renderer.setRenderOffset(playerPos.getX(), playerPos.getY());
	}

	@Override
	@SubscribeEvent
	public void handleKeyInput(CharacterInputEvent e) {
		Player player = room.getPlayer();
		if(e.getKey() == 'w') {
			player.setDirection(Direction.NORTH);
			player.setPosition(new Position(player.getPosition().getX(), player.getPosition().getY() - 5));
		} else if(e.getKey() == 'd') {
			player.setDirection(Direction.SOUTH);
			player.setPosition(new Position(player.getPosition().getX() + 5, player.getPosition().getY()));
		} else if(e.getKey() == 'a') {
			player.setDirection(Direction.WEST);
			player.setPosition(new Position(player.getPosition().getX() - 5, player.getPosition().getY()));
		} else if(e.getKey() == 's') {
			player.setDirection(Direction.EAST);
			player.setPosition(new Position(player.getPosition().getX(), player.getPosition().getY() + 5));
		}
	}

	@Override
	public IRenderer getRenderer() {
		return renderer;
	}

}
