package ai.arcblroth.boss.llama.ui;

import ai.arcblroth.boss.llama.LlamaStaticDefaults;
import ai.arcblroth.boss.llama.LlamaUtils;
import ai.arcblroth.boss.resource.InternalResource;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

import org.tbee.javafx.scene.layout.MigPane;

public class LlamaUI {
	
	private Stage stage;
	private MigPane rootPane;
	private Scene mainScene;

	public LlamaUI(Stage stage) {
		this.stage = stage;
		
		try {
			rootPane = FXMLLoader.load(new InternalResource("main.fxml").resolve());
			mainScene = new Scene(rootPane);
			mainScene.getStylesheets().add("stylesheet.css");
			stage.setTitle(LlamaStaticDefaults.TITLE);
			stage.setScene(mainScene);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void display() {
		
		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		
		stage.setX(primaryScreenBounds.getMinX() + primaryScreenBounds.getWidth() / 6);
        stage.setY(primaryScreenBounds.getMinY() + primaryScreenBounds.getHeight() / 6);
        stage.setWidth(primaryScreenBounds.getWidth() * 2 / 3);
        stage.setHeight(primaryScreenBounds.getHeight() * 2 / 3);

		stage.show();
		stage.centerOnScreen();
		
		ToggleGroup group = new ToggleGroup();
		((MigPane)LlamaUtils.getElementById(rootPane, "tiles")).add(new ToggleButton(), "grow");
		((MigPane)LlamaUtils.getElementById(rootPane, "tiles")).add(new ToggleButton(), "grow");
		((MigPane)LlamaUtils.getElementById(rootPane, "tiles")).add(new ToggleButton(), "grow");
	}
	
	public void shutdown() {
		stage.hide();
	}

}
