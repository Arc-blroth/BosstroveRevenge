package ai.arcblroth.boss.load;

import java.util.ArrayList;
import java.util.Arrays;

import ai.arcblroth.boss.engine.IEngine;
import ai.arcblroth.boss.engine.StepEvent;
import ai.arcblroth.boss.event.SubscribeEvent;
import ai.arcblroth.boss.in.KeyInputEvent;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.IRenderer;
import ai.arcblroth.boss.render.PixelAndTextGrid;
import ai.arcblroth.boss.render.PixelGrid;
import ai.arcblroth.boss.resource.PNGLoader;
import ai.arcblroth.boss.resource.ResourceLocation;
import ai.arcblroth.boss.util.PadUtils;
import ai.arcblroth.boss.util.TextureUtils;

public class LoadEngine implements IEngine {
	
	private double loadPercent = 0;
	private PixelAndTextGrid reallyBadGrid;
	private int arbitraryPaddingHeight = 8 * 2;
	private PixelGrid logo;
	
	public LoadEngine() {
		logo = TextureUtils.tintColor(PNGLoader.loadPNG(new ResourceLocation("bitmap.png")), new Color(41, 187, 255));
		reallyBadGrid = new PixelAndTextGrid(logo.getWidth(), logo.getHeight() + arbitraryPaddingHeight);
		reallyBadGrid = new PixelAndTextGrid(TextureUtils.overlay(logo, reallyBadGrid, 0, 0));
		updateStatus();
	}
	
	@Override
	@SubscribeEvent
	public void step(StepEvent e) {
		
	}

	@Override
	@SubscribeEvent
	public void handleKeyInput(KeyInputEvent e) {
		loadPercent = e.getKey();
		updateStatus();
	}

	@Override
	public IRenderer getRenderer() {
		return new IRenderer() {
			@Override
			public PixelAndTextGrid render() {
				return reallyBadGrid;
			}
		};
	}
	
	private void updateStatus() {
		reallyBadGrid.setCharacterRow(
				logo.getHeight() + arbitraryPaddingHeight - 2,
				PadUtils.stringToArrayList(PadUtils.centerPad(
						String.format("Loading - %.0f%%", loadPercent * 100)
						, reallyBadGrid.getWidth())),
				Color.BLACK,
				new Color(40, 237, 63)
		);
	}

}
