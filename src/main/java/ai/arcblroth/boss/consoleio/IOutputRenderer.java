package ai.arcblroth.boss.consoleio;

import ai.arcblroth.boss.render.*;

public interface IOutputRenderer {

	public void render(PixelGrid pg);
	public void clear();

}