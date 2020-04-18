package ai.arcblroth.boss.llama.io;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.io.IOutputRenderer;
import ai.arcblroth.boss.llama.LlamaUtils;
import ai.arcblroth.boss.render.PixelAndTextGrid;
import ai.arcblroth.boss.util.Pair;
import ai.arcblroth.boss.util.StaticDefaults;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Affine;

import java.util.List;

public class LlamaRenderer extends Canvas implements IOutputRenderer {

	private final Object renderingLock = new Object();
	private final Object inputLock = new Object();
	private PixelAndTextGrid pg;
	private Paint lastResetColor;
	private Pair<Integer, Integer> lastSize;
	private FxInputHandler inputHandler;

	public LlamaRenderer() {
		super();
		pg = new PixelAndTextGrid(1, 1);
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

	}

	@Override
	public void render(PixelAndTextGrid pg) {
		synchronized (renderingLock) {
			this.pg = pg;
			lastResetColor = LlamaUtils.colorToPaint(BosstrovesRevenge.instance().getResetColor());
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
				context.setFont(Font.font("Consolas", FontWeight.SEMI_BOLD, StaticDefaults.CHARACTER_WIDTH));
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
							context.fillText(rowTxt.get(colNum).toString(), offsetX + pixelWidth * colNum, offsetY + pixelWidth * (rowNum + 1.5));
						}
					}
				}

				context.setTransform(new Affine());
			} catch (Exception e) {
				e.printStackTrace();
			}
			renderingLock.notify();
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
		inputHandler.fireEvents();
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
