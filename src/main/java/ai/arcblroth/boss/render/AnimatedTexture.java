package ai.arcblroth.boss.render;

import java.util.ArrayList;
import ai.arcblroth.boss.register.IRegistrable;
import ai.arcblroth.boss.util.StaticDefaults;

public class AnimatedTexture extends Texture {
	
	private PixelGrid[] frames;
	private int currentFrame;
	
	public AnimatedTexture(PixelGrid[] frames) {
		super(frames[0]);
		this.frames = frames;
		this.currentFrame = 0;
	}
	
	public void advanceFrame() {
		currentFrame += 1;
		if(currentFrame >= frames.length) currentFrame = 0;
	}
	
	public Color get(int x, int y) {
		return getPixel(x, y);
	}
	
	public Color getPixel(int x, int y) {
		if (isCoordinateValid(x, y))
			return frames[currentFrame].get(x, y);
		else
			return StaticDefaults.RESET_COLOR;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public ArrayList<Color> getRow(int y) {
		return (ArrayList<Color>) frames[currentFrame].getRow(y).clone();
	}
	
	

}
