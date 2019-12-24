package ai.arcblroth.boss.consoleio;

import ai.arcblroth.boss.render.*;

public interface IOutputRenderer {

	public void render(PixelAndTextGrid pg);
	
	public void clear();
	
	public void displayFatalError(Throwable e);

	public void setDebugLine(String string);

}