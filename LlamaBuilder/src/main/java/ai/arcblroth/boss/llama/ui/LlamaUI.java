package ai.arcblroth.boss.llama.ui;

import ai.arcblroth.boss.llama.LlamaStaticDefaults;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tbee.javafx.scene.layout.MigPane;

public class LlamaUI {
	
	private Stage stage;
	private MigPane rootPane;
	private MigPane tileSelectorPane;
	private Scene mainScene;

	public LlamaUI(Stage stage) {
		this.stage = stage;
		rootPane = new MigPane("ins 0 0 0 0");
		
		tileSelectorPane = new MigPane("fillx");
		tileSelectorPane.setStyle("-fx-border-color: black; -fx-border-insets: 5; -fx-border-width: 3;");
		rootPane.add(tileSelectorPane, "left, width 20%, height 100%");
		
		Text selectATile = new Text("SELECT A TILE");
		tileSelectorPane.add(selectATile, "wrap");
		
		Button emptyTileBtn = new Button();
		emptyTileBtn.setText("empty");
		tileSelectorPane.add(emptyTileBtn, "width 100%");
		
		mainScene = new Scene(rootPane);
		stage.setTitle(LlamaStaticDefaults.TITLE);
		stage.setScene(mainScene);
	}
	
	public void display() {
		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		
		stage.setX(primaryScreenBounds.getMinX() + primaryScreenBounds.getWidth() / 6);
        stage.setY(primaryScreenBounds.getMinY() + primaryScreenBounds.getHeight() / 6);
        stage.setWidth(primaryScreenBounds.getWidth() * 2 / 3);
        stage.setHeight(primaryScreenBounds.getHeight() * 2 / 3);

		stage.show();
		stage.centerOnScreen();
	}
	
	public void shutdown() {
		stage.hide();
	}

}
