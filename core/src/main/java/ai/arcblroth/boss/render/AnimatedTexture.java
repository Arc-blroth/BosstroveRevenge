package ai.arcblroth.boss.render;

import java.util.ArrayList;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.register.IRegistrable;

public class AnimatedTexture extends MultiFrameTexture {
	
	private final int stepsPerFrame;
	private int currentStep;
	
	public AnimatedTexture(PixelGrid[] frames, int stepsPerFrame) {
		super(frames);
		this.stepsPerFrame = stepsPerFrame;
		this.currentStep = 0;
	}
	
	public synchronized void advanceFrame() {
		currentStep++;
		if(currentStep >= stepsPerFrame) {
			currentStep = 0;
			
			int nextFrame = getCurrentFrame() + 1;
			if(nextFrame >= getFrames()) {
				nextFrame = 0;
			}
			setCurrentFrame(nextFrame);
		}
	}

}
