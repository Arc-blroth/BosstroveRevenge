package ai.arcblroth.boss.game;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.engine.IEngine;
import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.StepEvent;
import ai.arcblroth.boss.event.SubscribeEvent;
import ai.arcblroth.boss.key.CharacterInputEvent;
import ai.arcblroth.boss.register.FloorTileRegistry;
import ai.arcblroth.boss.render.IRenderer;
import ai.arcblroth.boss.render.PixelAndTextGrid;

public class WorldEngine implements IEngine {
	
	private WorldRenderer renderer;
	private Room room;

	public WorldEngine() {
		this.room = new Room(40, 40);
		this.renderer = new WorldRenderer(room);
		room.getFloorTiles().set(0, 0, FloorTileRegistry.get().getTile("boss.sand"));
	}
	
	@Override
	@SubscribeEvent
	public void step(StepEvent e) {

	}

	@Override
	@SubscribeEvent
	public void handleKeyInput(CharacterInputEvent e) {
		if(e.getKey() == 'd') {
			renderer.setRenderOffset(renderer.getRenderOffsetX() + 1, renderer.getRenderOffsetY());
		} else if(e.getKey() == 'a') {
			renderer.setRenderOffset(renderer.getRenderOffsetX() - 1, renderer.getRenderOffsetY());
		} else if(e.getKey() == 'w') {
			renderer.setRenderOffset(renderer.getRenderOffsetX(), renderer.getRenderOffsetY() - 1);
		} else if(e.getKey() == 's') {
			renderer.setRenderOffset(renderer.getRenderOffsetX(), renderer.getRenderOffsetY() + 1);
		}
	}

	@Override
	public IRenderer getRenderer() {
		return renderer;
	}

}
