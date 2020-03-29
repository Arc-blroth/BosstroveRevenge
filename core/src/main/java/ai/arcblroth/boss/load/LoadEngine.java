package ai.arcblroth.boss.load;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.engine.IEngine;
import ai.arcblroth.boss.engine.StepEvent;
import ai.arcblroth.boss.game.WorldEngine;
import ai.arcblroth.boss.key.CharacterInputEvent;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.IRenderer;
import ai.arcblroth.boss.render.PixelAndTextGrid;
import ai.arcblroth.boss.render.PixelGrid;
import ai.arcblroth.boss.resource.InternalResource;
import ai.arcblroth.boss.resource.load.ResourceLoader;
import ai.arcblroth.boss.util.PadUtils;
import ai.arcblroth.boss.util.Pair;
import ai.arcblroth.boss.util.TextureUtils;

public class LoadEngine implements IEngine {
	
	private static final int arbitraryPaddingHeight = 8 * 2;
	private static final Color satBlue = new Color(41, 166, 255);
	private static final Color lightBlue = new Color(107, 190, 250);
	
	private LoadProcess loadProcess;
	
	private double blueInterpolation = 0;
	private double doneFadeoutAnimation = 0;
	
	private PixelAndTextGrid reallyBadGrid;
	private IRenderer renderer;
	private PixelGrid origLogo;
	private PixelGrid logo;
	
	public LoadEngine() {
		origLogo = TextureUtils.tintColorHSB(ResourceLoader.loadPNG(new InternalResource("bitmap.png")), satBlue);
		logo = new PixelGrid(origLogo);
		reallyBadGrid = new PixelAndTextGrid(logo.getWidth(), logo.getHeight() + arbitraryPaddingHeight);
		reallyBadGrid = new PixelAndTextGrid(TextureUtils.overlay(logo, reallyBadGrid, 0, 0));
		renderer = new IRenderer() {
			@Override
			public PixelAndTextGrid render() {
				return reallyBadGrid;
			}
		};
		loadProcess = new LoadProcess();
		loadProcess.start();
	}
	
	@Override
	public void step(StepEvent e) {
		if(!loadProcess.isDone()) {
			
			blueInterpolation += 0.01;
			if(blueInterpolation >= 1) blueInterpolation = -1;
			logo = TextureUtils.tintColor(origLogo,
					TextureUtils.interpolateHSB(satBlue, lightBlue, Math.abs(blueInterpolation))
			);
		} else {
			if(doneFadeoutAnimation <= 1) {
				doneFadeoutAnimation += 0.05;
				logo = TextureUtils.tintColor(origLogo, new Color(
						BosstrovesRevenge.instance().getResetColor().getRed(),
						BosstrovesRevenge.instance().getResetColor().getGreen(),
						BosstrovesRevenge.instance().getResetColor().getBlue(),
						(int)Math.round(doneFadeoutAnimation * 255)
				));
			} else {
				WorldEngine wee = new WorldEngine();
				BosstrovesRevenge.instance().setEngine(wee);
			}
		}
		updateStatus();
	}

	@Override
	public void handleKeyInput(CharacterInputEvent e) {
		
	}

	@Override
	public IRenderer getRenderer() {
		return renderer;
	}
	
	private void updateStatus() {
		reallyBadGrid = new PixelAndTextGrid(TextureUtils.overlay(logo, reallyBadGrid, 0, 0));
		Pair<Double, String> loadStatus = loadProcess.getProgressRecord();
		reallyBadGrid.setCharacterRow(
				logo.getHeight() +  arbitraryPaddingHeight - 2,
				PadUtils.stringToArrayList(PadUtils.centerPad(
						String.format("%s - %.0f%%", loadStatus.getSecond(), Math.min(loadStatus.getFirst() * 100, 100)),
						reallyBadGrid.getWidth())),
				BosstrovesRevenge.instance().getResetColor(),
				TextureUtils.interpolateRGB(
						new Color(40, 237, 63),
						BosstrovesRevenge.instance().getResetColor(),
						doneFadeoutAnimation
				)
		);
	}

}
