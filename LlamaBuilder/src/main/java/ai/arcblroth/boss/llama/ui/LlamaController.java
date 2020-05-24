package ai.arcblroth.boss.llama.ui;

import ai.arcblroth.boss.Llama;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class LlamaController {

	private Llama llama;
	private LlamaUI ui;

	public LlamaController(Llama llama, LlamaUI ui) {
		this.llama = llama;
		this.ui = ui;
	}

	@FXML
	public void handleRestartGame(ActionEvent actionEvent) {
		try {
			llama.reloadGameUnsafe();
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}

}
