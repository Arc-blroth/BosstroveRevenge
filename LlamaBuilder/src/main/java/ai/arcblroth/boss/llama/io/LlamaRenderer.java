package ai.arcblroth.boss.llama.io;

import ai.arcblroth.boss.Llama;
import ai.arcblroth.boss.io.IOutputRenderer;
import ai.arcblroth.boss.llama.LlamaUtils;
import ai.arcblroth.boss.render.PixelAndTextGrid;
import ai.arcblroth.boss.resource.InternalResource;
import ai.arcblroth.boss.util.Pair;
import ai.arcblroth.boss.util.StaticDefaults;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Affine;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LlamaRenderer extends Canvas implements IOutputRenderer {

	private final Object renderingLock = new Object();
	private Llama llama;
	private PixelAndTextGrid pg;
	private Font font;
	private Paint lastResetColor;
	private Pair<Integer, Integer> lastSize;
	private FxInputHandler inputHandler;

	public LlamaRenderer(Llama llama) {
		super();
		this.llama = llama;
		lastResetColor = new Color(0, 0, 0, 0);
		lastSize = new Pair<>(1, 1);
		inputHandler = new FxInputHandler();
		this.setOnKeyPressed(event -> inputHandler.handleInput(event.getCode(), true));
		this.setOnKeyReleased(event -> inputHandler.handleInput(event.getCode(), false));
		this.setOnMouseClicked(event -> requestFocus());
	}

	@Override
	public boolean isResizable() {
		return true;
	}

	@Override
	public double prefWidth(double height) {
		return getWidth();
	}

	@Override
	public double prefHeight(double width) {
		return getHeight();
	}

	@Override
	public void init() {
		if(Font.getFamilies().contains("Consolas")) {
			font = Font.font("Consolas", FontWeight.SEMI_BOLD, StaticDefaults.CHARACTER_WIDTH);
		} else {
			Font tempFont = null;
			try {
				InputStream fontStream = new InternalResource("font/RobotoMono-Regular.ttf").resolve().openStream();
				tempFont = Font.loadFont(fontStream, StaticDefaults.CHARACTER_WIDTH - 1);
				fontStream.close();
			} catch(Exception e) {
				tempFont = null;
			}
			if(tempFont != null) {
				font = tempFont;
			} else {
				Logger.getLogger("LlamaRenderer").log(Level.WARNING, "Could not load built-in font, falling back to system default.");
				font = Font.font("Monospace", FontWeight.SEMI_BOLD, StaticDefaults.CHARACTER_WIDTH);
			}
		}
	}

	@Override
	public void render(PixelAndTextGrid pg) {
		synchronized (renderingLock) {
			this.pg = pg;
			lastResetColor = LlamaUtils.colorToPaint(LlamaUtils.getResetColorFromGameInstance(llama.getGameInstance()));
			try {
				renderingLock.wait();
			} catch (InterruptedException e) {}
			lastSize = new Pair<>((int)Math.floor(this.getWidth()/StaticDefaults.CHARACTER_WIDTH*2), (int)Math.floor(this.getHeight()/StaticDefaults.CHARACTER_WIDTH*2));
		}
	}

	public void fxRender() {
		synchronized (renderingLock) {
			try {
				GraphicsContext context = this.getGraphicsContext2D();
				context.setFill(lastResetColor);
				context.setFont(font);
				context.fillRect(0, 0, getWidth(), getHeight());

				// Only transform the height, as the layout engine usually makes the width larger than it should
				double pixelWidth = StaticDefaults.CHARACTER_WIDTH / 2D;
				double roundedHeight = (lastSize.getSecond() * StaticDefaults.CHARACTER_WIDTH) / 2F;
				double halfCharWidth = StaticDefaults.CHARACTER_WIDTH / 2F;
				context.transform(new Affine(Affine.scale(1, 1D + 2 * halfCharWidth / roundedHeight)));
				context.transform(new Affine(Affine.translate(pixelWidth / -2D, pixelWidth / -2D)));

				double offsetX = (getWidth() - (pg.getWidth() * pixelWidth)) / 2D;
				double offsetY = (getHeight() - (pg.getHeight() * pixelWidth)) / 2D;

				for (int rowNum = 0; rowNum < (pg.getHeight() / 2) * 2; rowNum += 2) {
					List<ai.arcblroth.boss.render.Color> row1 = pg.getRow(rowNum);
					List<ai.arcblroth.boss.render.Color> row2 = pg.getRow(rowNum + 1);

					for (int colNum = 0; colNum < pg.getWidth(); colNum++) {
						if(!row1.get(colNum).equals(lastResetColor)) {
							context.setFill(LlamaUtils.colorToPaint(row1.get(colNum)));
							context.fillRect(offsetX + pixelWidth * colNum, offsetY + pixelWidth * rowNum, pixelWidth + 1, pixelWidth + 1);
						}
						if(!row2.get(colNum).equals(lastResetColor)) {
							context.setFill(LlamaUtils.colorToPaint(row2.get(colNum)));
							context.fillRect(offsetX + pixelWidth * colNum, offsetY + pixelWidth * (rowNum + 1), pixelWidth + 1, pixelWidth + 1);
						}
					}
				}
				for (int rowNum = 0; rowNum < (pg.getHeight() / 2) * 2; rowNum += 2) {
					List<Character> rowTxt = pg.getCharacterRow(rowNum);

					for (int colNum = 0; colNum < pg.getWidth(); colNum++) {
						if(rowTxt.get(colNum) != StaticDefaults.RESET_CHAR) {
							context.setFill(LlamaUtils.colorToPaint(pg.getColorsAt(colNum, rowNum).getSecond()));
							context.fillRect(offsetX + pixelWidth * colNum, offsetY + pixelWidth * rowNum, pixelWidth + 1, 2 * pixelWidth + 1);
							context.setFill(LlamaUtils.colorToPaint(pg.getColorsAt(colNum, rowNum).getFirst()));
							context.fillText(rowTxt.get(colNum).toString(), offsetX + pixelWidth * colNum, offsetY + pixelWidth * (rowNum + 1.75));
						}
					}
				}

				context.setTransform(new Affine());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				renderingLock.notify();
			}
		}
	}

	@Override
	public void clear() {

	}

	@Override
	public void displayFatalError(Throwable e) {

	}

	@Override
	public void pollInput() {
		inputHandler.fireEvents(llama);
	}

	@Override
	public Pair<Integer, Integer> getSize() {
		synchronized (renderingLock) {
			return lastSize;
		}
	}

	@Override
	public boolean isShowingFPS() {
		return false;
	}

	@Override
	public void setShowingFPS(boolean showFPS) {

	}

}
