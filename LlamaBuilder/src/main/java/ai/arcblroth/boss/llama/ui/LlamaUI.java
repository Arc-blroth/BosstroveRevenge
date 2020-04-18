package ai.arcblroth.boss.llama.ui;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.llama.LlamaStaticDefaults;
import ai.arcblroth.boss.llama.LlamaUtils;
import ai.arcblroth.boss.llama.io.LlamaRenderer;
import ai.arcblroth.boss.resource.InternalResource;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tbee.javafx.scene.layout.MigPane;

import java.io.IOException;

public class LlamaUI {
	
	private Stage stage;
	private MigPane rootPane;
	private Scene mainScene;
	private LlamaRenderer levelRenderer;

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

		this.levelRenderer = new LlamaRenderer();
	}
	
	public void display() {
		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		
		stage.setX(primaryScreenBounds.getMinX() + primaryScreenBounds.getWidth() / 6);
        stage.setY(primaryScreenBounds.getMinY() + primaryScreenBounds.getHeight() / 6);
        stage.setWidth(primaryScreenBounds.getWidth() * 2 / 3);
        stage.setHeight(primaryScreenBounds.getHeight() * 2 / 3);

		stage.show();
		stage.setOnCloseRequest((event) -> {
			BosstrovesRevenge.instance().shutdown(0);
			stage.close();
		});
		stage.centerOnScreen();

		MigPane tiles = ((MigPane)LlamaUtils.getElementById(rootPane, "tiles"));

		ToggleGroup group = new ToggleGroup();
		tiles.add(new ToggleButton(), "grow");
		tiles.add(new ToggleButton(), "grow");
		tiles.add(new ToggleButton(), "grow");

		MigPane levelRendererPane = ((MigPane)LlamaUtils.getElementById(rootPane, "levelRenderer"));
		levelRendererPane.add(levelRenderer, "grow");
		levelRenderer.widthProperty().bind(rootPane.widthProperty().subtract(tiles.widthProperty()));
		levelRenderer.heightProperty().bind(rootPane.heightProperty());

		new AnimationTimer() {
			@Override
			public void handle(long now) {
				levelRenderer.fxRender();
			}
		}.start();

	}

	public LlamaRenderer getLevelRenderer() {
		return levelRenderer;
	}
}
