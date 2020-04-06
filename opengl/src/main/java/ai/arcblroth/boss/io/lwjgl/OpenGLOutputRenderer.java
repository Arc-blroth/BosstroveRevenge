package ai.arcblroth.boss.io.lwjgl;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.Relauncher;
import ai.arcblroth.boss.crash.CrashReportGenerator;
import ai.arcblroth.boss.io.IOutputRenderer;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.PixelAndTextGrid;
import ai.arcblroth.boss.resource.ExternalResource;
import ai.arcblroth.boss.resource.InternalResource;
import ai.arcblroth.boss.resource.Resource;
import ai.arcblroth.boss.util.Pair;
import ai.arcblroth.boss.util.StaticDefaults;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ai.arcblroth.boss.io.lwjgl.OpenGLUtils.rgbToVector;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL30.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL30.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL30.glClear;
import static org.lwjgl.opengl.GL30.glClearColor;
import static org.lwjgl.opengl.GL30.glViewport;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class OpenGLOutputRenderer implements IOutputRenderer {

	private static final long BYTES_IN_MEGABYTE = 1000000;
	
	private Throwable error;
	private Window window;
	private Shader shader;
	private PixelModel model;
	private StbFontManager fontManager;
	private GlfwInputHandler inputHandler;
	
	private Logger logger;
	
	private boolean showFPS = true;
	private double fps = 1;
	private long lastRenderTime;
	private long fullscreenKeyTimeout = 0;
	private volatile Pair<Integer, Integer> lastSize;
	
	public OpenGLOutputRenderer() {
		try {
			logger = Logger.getLogger("OpenGLOutputRenderer");
			window = new Window("Bosstrove's Revenge", 0, 0, OpenGLUtils.DEFAULT_FULLSCREEN);
			inputHandler = new GlfwInputHandler();
			
			lastRenderTime = System.currentTimeMillis();
			lastSize = new Pair<Integer, Integer>(0, 0);
		} catch (Exception e) {
			System.err.println("Could not init display, aborting launch...");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	@Override
	public void init() {
		window.init();
		glfwMakeContextCurrent(window.getHandle());
		
		try {
			shader = new Shader(new InternalResource("shader/pixel.vert"), new InternalResource("shader/pixel.frag"));
			
			Resource font = new InternalResource("font/RobotoMono-Medium.ttf");
			//Use Consolas if on windows, Roboto Mono otherwise
			if(Relauncher.IS_WINDOWS && !Relauncher.IS_CYGWIN && !Relauncher.IS_MINGW_XTERM) {
				Resource consolas = new ExternalResource("C:\\Windows\\Fonts\\consola.ttf");
				if(consolas.exists()) {
					logger.log(Level.INFO, "Using Consolas(R) as font.");
					font = consolas;
				}
			}
			fontManager = new StbFontManager(font);
			fontManager.init();
		} catch (Exception e) {
			e.printStackTrace();
			BosstrovesRevenge.instance().shutdown(-1);
		}
		
		model = new PixelModel();
		
		glfwSetKeyCallback(window.getHandle(), (long windowHandle, int key, int scancode, int action, int mods) -> {
			//Fullscreen handling
			if(key == GLFW_KEY_F11 && fullscreenKeyTimeout == 0) {
				fullscreenKeyTimeout = StaticDefaults.DEFAULT_KEYBIND_DELAY * 3;
				window.setFullscreen(!window.isFullscreen());
			}

			try {inputHandler.handleInput(key, action);} catch (Throwable e) {}
		});
	}

	public void render(PixelAndTextGrid pg) {
		
		if(error != null) {
			dispose();
			return;
		}

		fullscreenKeyTimeout = Math.max(fullscreenKeyTimeout - 1, 0);
		
		//if(pg != null) {
			if(!(System.getProperty(Relauncher.FORCE_NORENDER) != null && System.getProperty(Relauncher.FORCE_NORENDER).equals("true"))) {

				Color clearColor = BosstrovesRevenge.instance().getResetColor();
				Vector4f clearColorVec = OpenGLUtils.rgbToVector(clearColor);
				glClearColor(clearColorVec.x, clearColorVec.y, clearColorVec.z, 1.0F);
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		        glEnable(GL_BLEND);
				
				if (window.isResized()) {
					glViewport(0, 0, window.getWidth(), window.getHeight());
					window.setResized(false);
				}
				
				glUseProgram(shader.getHandle());
				glUniform1i(glGetUniformLocation(shader.getHandle(), "texture1"), 0);

				int width = window.getWidth();
				int height = window.getHeight();

				lastSize = new Pair<Integer, Integer>(
						2 * (int)Math.round((double)width / (double)StaticDefaults.CHARACTER_WIDTH),
						2 * (int)Math.round((double)height / ((double)StaticDefaults.CHARACTER_HEIGHT / 2D))
				);

				float roundedWidth = (lastSize.getFirst() * StaticDefaults.CHARACTER_WIDTH) / 2F;
				float roundedHeight = (lastSize.getSecond() * StaticDefaults.CHARACTER_WIDTH) / 2F;
				float halfCharWidth = StaticDefaults.CHARACTER_WIDTH / 2F;

				shader.setMatrix4f("projection", new Matrix4f().ortho(
						-roundedWidth - halfCharWidth,
						roundedWidth - halfCharWidth,
						-roundedHeight + halfCharWidth,
						roundedHeight + halfCharWidth,
						0, 1));
				Matrix4f scaledModelMatrix = new Matrix4f().scale(StaticDefaults.CHARACTER_WIDTH, StaticDefaults.CHARACTER_WIDTH, 1F);

				// Render each row like a printer would
				// Text is always printed on top of background stuff.
				for (int rowNum = 0; rowNum < (pg.getHeight() / 2) * 2; rowNum += 2) {
					ArrayList<Color> row1 = pg.getRow(rowNum);
					ArrayList<Color> row2 = pg.getRow(rowNum + 1);

					for (int colNum = 0; colNum < pg.getWidth(); colNum++) {
						// Draw ordinary pixels
						// If pixel color == background color, don't draw it!
						if(!row1.get(colNum).equals(clearColor)) {
							drawPixel(
								new Matrix4f(scaledModelMatrix).translate(
									(-pg.getWidth()/2F + colNum),
									( pg.getHeight()/2F - rowNum),
									0
								).scale(0.5F),
								row1.get(colNum)
							);
						}
						if(!row2.get(colNum).equals(clearColor)) {
							drawPixel(
								new Matrix4f(scaledModelMatrix).translate(
										(-pg.getWidth()/2F + colNum),
										( pg.getHeight()/2F - rowNum - 1),
										0
								).scale(0.5F),
								row2.get(colNum)
							);
						}
					}
				}
				for (int rowNum = 0; rowNum < (pg.getHeight() / 2) * 2; rowNum += 2) {
					ArrayList<Character> rowTxt = pg.getCharacterRow(rowNum);

					for (int colNum = 0; colNum < pg.getWidth(); colNum++) {
						if(rowTxt.get(colNum) != StaticDefaults.RESET_CHAR) {
							// Draw characters on a background color
							drawCharacter(
									new Matrix4f(scaledModelMatrix).translate(
											(-pg.getWidth()/2F + colNum - 0.5F),
											( pg.getHeight()/2F - rowNum - 1.25F),
											0
									).scale(1F, -1F, 1F),
									new Matrix4f(scaledModelMatrix).translate(
											(-pg.getWidth()/2F + colNum),
											( pg.getHeight()/2F - rowNum - 0.5F),
											0
									).scale(0.5F, 1F, 1F),
									pg.getColorsAt(colNum, rowNum),
									rowTxt.get(colNum)
							);
						}
					}
				}

				//FPS + memory
				if(showFPS) {

					//For inverted-ness
					glBlendFuncSeparate(GL_ONE_MINUS_DST_COLOR, GL_ONE_MINUS_SRC_ALPHA, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
					shader.setBool("invertTexture", true);

					String fpsString = String.format("%.0f FPS", fps);
					for(int fpsCharIndex = 0; fpsCharIndex < fpsString.length(); fpsCharIndex++) {
						drawCharacter(
							new Matrix4f(scaledModelMatrix).translate(
									(-(float)width / StaticDefaults.CHARACTER_WIDTH + fpsCharIndex - 0.25F),
									( (float)height / StaticDefaults.CHARACTER_WIDTH - 1F),
									0
							).scale(1F, -1F, 1F),
							new Matrix4f(scaledModelMatrix).translate(
									(-(float)width / StaticDefaults.CHARACTER_WIDTH + fpsCharIndex),
									( (float)height / StaticDefaults.CHARACTER_WIDTH - 0.5F),
									0
							).scale(0.5F, 1F, 1F),
							new Pair<Color, Color>(Color.WHITE, null),
							fpsString.charAt(fpsCharIndex)
						);
					}

					String memString = String.format("%s MB / %s MB",
							Runtime.getRuntime().freeMemory() / BYTES_IN_MEGABYTE,
							Runtime.getRuntime().totalMemory() / BYTES_IN_MEGABYTE
					);
					for(int memCharIndex = 0; memCharIndex < memString.length(); memCharIndex++) {
						drawCharacter(
							new Matrix4f(scaledModelMatrix).translate(
									((float)width / StaticDefaults.CHARACTER_WIDTH - memCharIndex - 1.75F),
									((float)height / StaticDefaults.CHARACTER_WIDTH - 1F),
									0
							).scale(1F, -1F, 1F),
							new Matrix4f(scaledModelMatrix).translate(
									((float)width / StaticDefaults.CHARACTER_WIDTH - memCharIndex),
									((float)height / StaticDefaults.CHARACTER_WIDTH - 0.5F),
									0
							).scale(0.5F, 1F, 1F),
							new Pair<Color, Color>(Color.WHITE, null),
							memString.charAt(memString.length() - 1 - memCharIndex)
						);
					}

					shader.setBool("invertTexture", false);
					glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

				}
				
				//RENDER!
				glfwSwapBuffers(window.getHandle());
				glfwPollEvents();
				
				if(window.windowShouldClose()) {
					BosstrovesRevenge.instance().shutdown(0);
				}
				
				//FPS Benchmarking
				long currTime = System.currentTimeMillis();
				fps = 1000D / (currTime - lastRenderTime);
				lastRenderTime = currTime;
				//System.out.printf("FPS: %.2f\n", fps);
			}
		//}
	}
	
	private void drawPixel(Matrix4f modelMatrix, Color color) {
		shader.setMatrix4f("model", modelMatrix);
		shader.setVector4f("color", rgbToVector(color));
		model.render();
	}
	
	private void drawCharacter(Matrix4f characterModelMatrix, Matrix4f backgroundModelMatrix, Pair<Color, Color> colors, char character) {
		// Draw background first
		if(colors.getSecond() != null) {
			shader.setMatrix4f("model", backgroundModelMatrix);
			shader.setVector4f("color", rgbToVector(colors.getSecond()));
			model.render();
		}
		
		// Draw the character second, because OpenGL
		if(colors.getFirst() != null) {
			shader.setBool("useTexture", true);
			shader.setMatrix4f("model", characterModelMatrix);
			shader.setVector4f("color", rgbToVector(colors.getFirst()));
			fontManager.renderCharacter(character);
			shader.setBool("useTexture", false);
		}
	}
	
	@Override
	public void dispose() {
		model.dispose();
		fontManager.dispose();
		glfwMakeContextCurrent(NULL);
		glfwDestroyWindow(window.getHandle());
		glfwSetErrorCallback(null).free();
		glfwTerminate();
	}

	public void displayFatalError(Throwable e) {
		error = e;
		System.out.println(CrashReportGenerator.generateCrashReport(e));
	}
	
	public void clear() {
		if(!(System.getProperty(Relauncher.FORCE_NORENDER) != null && System.getProperty(Relauncher.FORCE_NORENDER).equals("true"))) {
			
		}
	}
	
	public double getFps() {
		return fps;
	}
	
	@Override
	public void pollInput() {
		inputHandler.fireEvents();
	}
	
	@Override
	public Pair<Integer, Integer> getSize() {
		return lastSize;
	}

	@Override
	public boolean isShowingFPS() {
		return showFPS;
	}

	@Override
	public void setShowingFPS(boolean showFPS) {
		this.showFPS = showFPS;
	}

}