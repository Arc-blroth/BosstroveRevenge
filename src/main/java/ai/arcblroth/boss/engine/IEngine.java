package ai.arcblroth.boss.engine;

import ai.arcblroth.boss.in.KeyInputEvent;
import ai.arcblroth.boss.render.IRenderer;

public interface IEngine {
	
	public void step(StepEvent e);
	
	public void handleKeyInput(KeyInputEvent e);
	
	public IRenderer getRenderer();
	
	
}
