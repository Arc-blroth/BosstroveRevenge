package ai.arcblroth.boss.game;

import ai.arcblroth.boss.engine.IEngine;
import ai.arcblroth.boss.engine.StepEvent;
import ai.arcblroth.boss.event.SubscribeEvent;
import ai.arcblroth.boss.in.KeyInputEvent;
import ai.arcblroth.boss.render.IRenderer;
import ai.arcblroth.boss.render.PixelAndTextGrid;

public class WorldEngine implements IEngine {
	
	public WorldEngine() {
		
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
		return new IRenderer() {
			@Override
			public PixelAndTextGrid render() {
				return new PixelAndTextGrid(10, 10);
			}
		};
	}

}
