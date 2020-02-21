package ai.arcblroth.boss.io;

import ai.arcblroth.boss.render.*;
import ai.arcblroth.boss.util.Pair;

public interface IOutputRenderer {

	public void init();
	
	public void render(PixelAndTextGrid pg);
	
	public void clear();
	
	public void displayFatalError(Throwable e);

	public void setDebugLine(String string);

	public void pollInput();
	
	public default void dispose() {};
	
	public Pair<Integer, Integer> getSize();

}