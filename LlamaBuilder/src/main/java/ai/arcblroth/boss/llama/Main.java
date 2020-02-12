package ai.arcblroth.boss.llama;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		Llama llama = new Llama(stage);
		llama.run();
	}

}
