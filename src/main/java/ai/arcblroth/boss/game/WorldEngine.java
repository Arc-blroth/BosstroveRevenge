package ai.arcblroth.boss.game;

import ai.arcblroth.boss.engine.IEngine;
import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.StepEvent;
import ai.arcblroth.boss.event.SubscribeEvent;
import ai.arcblroth.boss.in.KeyInputEvent;
import ai.arcblroth.boss.register.FloorTileRegistry;
import ai.arcblroth.boss.render.IRenderer;
import ai.arcblroth.boss.render.PixelAndTextGrid;

public class WorldEngine implements IEngine {
	
	private WorldRenderer renderer;
	private Room room;

	public WorldEngine() {
		this.room = new Room(10, 10);
		this.renderer = new WorldRenderer(room);
	}
	
	@Override
	@SubscribeEvent
	public void step(StepEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	@SubscribeEvent
	public void handleKeyInput(KeyInputEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public IRenderer getRenderer() {
		return renderer;
	}

}
