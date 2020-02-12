package ai.arcblroth.boss.llama.ui;

import java.awt.Canvas;

public class LlamaCanvas extends Canvas {
	
	private volatile boolean isRepainting;
	
	public LlamaCanvas() {
		isRepainting = false;
		setIgnoreRepaint(true);
	}
	
	public void paint() {
		
	}
	
}
