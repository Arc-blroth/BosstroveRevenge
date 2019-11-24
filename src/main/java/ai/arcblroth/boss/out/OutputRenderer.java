package ai.arcblroth.boss.out;

import ai.arcblroth.boss.render.*;

public interface OutputRenderer {

	public void render(PixelGrid pg);
	public void clear();

}