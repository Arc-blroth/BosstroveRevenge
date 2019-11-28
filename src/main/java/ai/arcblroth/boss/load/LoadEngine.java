package ai.arcblroth.boss.load;

import ai.arcblroth.boss.engine.IEngine;
import ai.arcblroth.boss.engine.StepEvent;
import ai.arcblroth.boss.event.SubscribeEvent;
import ai.arcblroth.boss.in.KeyInputEvent;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.IRenderer;
import ai.arcblroth.boss.render.PixelGrid;
import ai.arcblroth.boss.resource.PNGLoader;
import ai.arcblroth.boss.resource.ResourceLocation;
import ai.arcblroth.boss.util.TextureUtils;

public class LoadEngine implements IEngine {
	
	private PixelGrid reallyBadGrid;

	@Override
	@SubscribeEvent
	public void step(StepEvent e) {
		reallyBadGrid = TextureUtils.tintColor(PNGLoader.loadPNG(new ResourceLocation("bitmap.png")), new Color(41, 187, 255));
	}

	@Override
	@SubscribeEvent
	public void handleKeyInput(KeyInputEvent e) {
		
	}

	@Override
	public IRenderer getRenderer() {
		return new IRenderer() {
			@Override
			public PixelGrid render() {
				return reallyBadGrid;
			}
		};
	}

}
