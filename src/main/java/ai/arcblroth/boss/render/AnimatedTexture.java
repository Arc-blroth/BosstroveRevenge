package ai.arcblroth.boss.render;

import java.util.ArrayList;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.register.IRegistrable;

public class AnimatedTexture extends Texture {
	
	private final PixelGrid[] frames;
	private final int stepsPerFrame;
	private int currentFrame;
	private int currentStep;
	
	public AnimatedTexture(PixelGrid[] frames, int stepsPerFrame) {
		super(frames[0]);
		this.frames = frames;
		this.stepsPerFrame = stepsPerFrame;
		this.currentFrame = 0;
		this.currentStep = 0;
	}
	
	public synchronized void advanceFrame() {
		currentStep++;
		if(currentStep >= stepsPerFrame) {
			currentStep = 0;
			
			currentFrame += 1;
			if(currentFrame >= frames.length) {
				currentFrame = 0;
			}
		}
	}
	
	public Color get(int x, int y) {
		return getPixel(x, y);
	}
	
	public Color getPixel(int x, int y) {
		if (isCoordinateValid(x, y))
			return frames[currentFrame].get(x, y);
		else
			return BosstrovesRevenge.instance().getResetColor();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public ArrayList<Color> getRow(int y) {
		return (ArrayList<Color>) frames[currentFrame].getRow(y).clone();
	}
	
	

}
