package ai.arcblroth.boss.render;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.util.StaticDefaults;

import java.util.Arrays;
import java.util.List;

public class MultiFrameTexture extends Texture {
	
	private final PixelGrid[] frames;
	private volatile int currentFrame;
	
	public MultiFrameTexture(PixelGrid[] frames) {
		super(frames[0]);
		this.frames = frames;
		this.currentFrame = 0;
	}
	
	public void setCurrentFrame(int frame) {
		//if(frame < 0 || frame > frames.length - 1) throw new IllegalArgumentException("Invalid frame number");
		
		if(frame < 0) frame = 0;
		else if(frame > frames.length - 1) frame = frames.length - 1;
		
		currentFrame = frame;
	}
	
	public int getCurrentFrame() {
		return currentFrame;
	}
	
	public int getFrames() {
		return frames.length;
	}
	
	private PixelGrid getCurrentPixelGridOrDefault() {
		if(currentFrame < 0 || currentFrame > frames.length - 1) return StaticDefaults.DEFAULT_TEXTURE;
		else if(frames[currentFrame] == null) return StaticDefaults.DEFAULT_TEXTURE;
		else return frames[currentFrame];
	}
	
	public Color get(int x, int y) {
		return getPixel(x, y);
	}
	
	public Color getPixel(int x, int y) {
		if (isCoordinateValid(x, y))
			return getCurrentPixelGridOrDefault().get(x, y);
		else
			return BosstrovesRevenge.instance().getResetColor();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Color> getRow(int y) {
		return Arrays.asList((Color[])getCurrentPixelGridOrDefault().getRow(y).toArray());
	}
	
	

}
