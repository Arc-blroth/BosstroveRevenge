package ai.arcblroth.boss.render;

import ai.arcblroth.boss.register.IRegistrable;

import java.util.Arrays;
import java.util.List;

public class Texture extends PixelGrid implements IRegistrable<Texture> {
	
	public Texture(PixelGrid pg) {
		super(pg);
	}
	
	public Texture(int width, int height) {
		super(width, height);
	}
	
	/**
	 * @throws UnsupportedOperationException Textures are immutable.
	 */
	@Override
	public Color set(int x, int y, Color element) {
		throw new UnsupportedOperationException("Textures are immutable.");
	}
	
	/**
	 * Returns row {@code y} of pixels from this texture.
	 * @param y Row of this texture to get.
	 * @return a <b>cloned</b> copy of row {@code y} of this texture.
	 */
	@Override
	public List<Color> getRow(int y) {
		return Arrays.asList((Color[])super.getRow(y).toArray());
	}
	
	/**
	 * @throws UnsupportedOperationException Textures are immutable.
	 */
	@Override
	public void setRow(int y, List<Color> row) {
		throw new UnsupportedOperationException("Textures are immutable.");
	}
	
	/**
	 * @throws UnsupportedOperationException Textures are immutable.
	 */
	@Override
	public void setPixel(int x, int y, Color c) {
		throw new UnsupportedOperationException("Textures are immutable.");
	}

}
