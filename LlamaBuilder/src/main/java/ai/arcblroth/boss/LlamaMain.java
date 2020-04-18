package ai.arcblroth.boss;

import javafx.application.Application;
import javafx.stage.Stage;

public class LlamaMain extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		Environment.setLoggingPattern();
		Llama llama = new Llama(stage);
		llama.run();
	}

}
