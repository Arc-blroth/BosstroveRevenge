package ai.arcblroth.boss.consoleio;

import ai.arcblroth.boss.render.*;

public interface IOutputRenderer {

	public void render(PixelAndTextGrid pg);
	public void clear();

}