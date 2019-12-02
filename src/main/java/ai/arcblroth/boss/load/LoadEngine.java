package ai.arcblroth.boss.load;

import java.util.ArrayList;
import java.util.Arrays;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.engine.IEngine;
import ai.arcblroth.boss.engine.StepEvent;
import ai.arcblroth.boss.engine.tile.EmptyFloorTile;
import ai.arcblroth.boss.engine.tile.EmptyWallTile;
import ai.arcblroth.boss.event.SubscribeEvent;
import ai.arcblroth.boss.game.WorldEngine;
import ai.arcblroth.boss.in.KeyInputEvent;
import ai.arcblroth.boss.register.FloorTileRegistry;
import ai.arcblroth.boss.register.WallTileRegistry;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.IRenderer;
import ai.arcblroth.boss.render.PixelAndTextGrid;
import ai.arcblroth.boss.render.PixelGrid;
import ai.arcblroth.boss.resource.PNGLoader;
import ai.arcblroth.boss.resource.Resource;
import ai.arcblroth.boss.util.OutputDefaults;
import ai.arcblroth.boss.util.PadUtils;
import ai.arcblroth.boss.util.TextureUtils;

public class LoadEngine implements IEngine {

	private double loadPercent = 0;
	private double doneFadeoutAnimation = 0;
	
	private PixelAndTextGrid reallyBadGrid;
	private IRenderer renderer;
	private static final int arbitraryPaddingHeight = 8 * 2;
	private PixelGrid origLogo;
	private PixelGrid logo;
	
	public LoadEngine() {
		origLogo = TextureUtils.tintColor(PNGLoader.loadPNG(new Resource("bitmap.png")), new Color(41, 166, 255));
		logo = new PixelGrid(origLogo);
		reallyBadGrid = new PixelAndTextGrid(logo.getWidth(), logo.getHeight() + arbitraryPaddingHeight);
		reallyBadGrid = new PixelAndTextGrid(TextureUtils.overlay(logo, reallyBadGrid, 0, 0));
		renderer = new IRenderer() {
			@Override
			public PixelAndTextGrid render() {
				return reallyBadGrid;
			}
		};
	}
	
	@Override
	@SubscribeEvent
	public void step(StepEvent e) {
		if(loadPercent < 1) {
			//This will be changed later
			if(loadPercent == 0.1) {
				FloorTileRegistry.get().register("empty", new EmptyFloorTile());
				WallTileRegistry.register("empty", new EmptyWallTile());
			}
			loadPercent += 0.1;
		} else {
			if(doneFadeoutAnimation <= 1) {
				doneFadeoutAnimation += 0.05;
				logo = TextureUtils.tintColorRGB(origLogo, new Color(
						OutputDefaults.RESET_COLOR.getRed(),
						OutputDefaults.RESET_COLOR.getGreen(),
						OutputDefaults.RESET_COLOR.getBlue(),
						(int)Math.round(doneFadeoutAnimation * 255)
				));
				reallyBadGrid = new PixelAndTextGrid(TextureUtils.overlay(logo, reallyBadGrid, 0, 0));
			} else {
				WorldEngine wee = new WorldEngine();
				BosstrovesRevenge.get().setEngine(wee);
			}
		}
		updateStatus();
	}

	@Override
	@SubscribeEvent
	public void handleKeyInput(KeyInputEvent e) {
		
	}

	@Override
	public IRenderer getRenderer() {
		return renderer;
	}
	
	private void updateStatus() {
		reallyBadGrid.setCharacterRow(
				logo.getHeight() +  arbitraryPaddingHeight - 2,
				PadUtils.stringToArrayList(PadUtils.centerPad(
						String.format("Loading - %.0f%%", Math.min(loadPercent * 100, 100)),
						reallyBadGrid.getWidth())),
				OutputDefaults.RESET_COLOR,
				TextureUtils.interpolateRGB(
						new Color(40, 237, 63),
						OutputDefaults.RESET_COLOR,
						doneFadeoutAnimation
				)
		);
	}

}
