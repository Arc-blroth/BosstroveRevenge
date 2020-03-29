package ai.arcblroth.boss.engine;

import ai.arcblroth.boss.key.CharacterInputEvent;
import ai.arcblroth.boss.render.IRenderer;

public interface IEngine {
	
	public void step(StepEvent e);
	
	public void handleKeyInput(CharacterInputEvent e);
	
	public IRenderer getRenderer();
	
	
}
