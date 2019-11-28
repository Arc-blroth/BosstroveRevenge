package ai.arcblroth.boss.consoleio;

import ai.arcblroth.boss.render.*;

public interface OutputRenderer {

	public void render(PixelGrid pg);
	public void clear();

}