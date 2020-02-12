package ai.arcblroth.boss.llama;

import ai.arcblroth.boss.llama.ui.LlamaUI;
import javafx.stage.Stage;

public final class Llama extends Thread {
	
	private boolean isRunning = false;
	private LlamaUI ui;
	
	Llama(Stage stage) {
		this.ui = new LlamaUI(stage);
	}
	
	public void run() {
		if(isRunning) return;
		isRunning = true;
		ui.display();
	}
	
}
