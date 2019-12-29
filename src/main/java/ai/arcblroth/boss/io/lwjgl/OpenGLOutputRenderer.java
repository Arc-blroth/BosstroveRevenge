package ai.arcblroth.boss.io.lwjgl;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.Relauncher;
import ai.arcblroth.boss.io.IOutputRenderer;
import ai.arcblroth.boss.render.*;
import ai.arcblroth.boss.resource.ExternalResource;
import ai.arcblroth.boss.resource.InternalResource;
import ai.arcblroth.boss.resource.Resource;
import ai.arcblroth.boss.util.Pair;
import ai.arcblroth.boss.util.StaticDefaults;
import ai.arcblroth.boss.util.TextureUtils;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import static ai.arcblroth.boss.io.lwjgl.OpenGLUtils.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OpenGLOutputRenderer implements IOutputRenderer {
	
	private Throwable error;
	private Window window;
	private Shader shader;
	private PixelModel model;
	private StbFontManager fontManager;
	private GlfwInputHandler inputHandler;
	
	private Logger logger;
	private Logger debugLinelogger;
	
	private static final boolean SHOW_FPS = true;
	private double fps = 1;
	private long lastRenderTime;
	private static final long BYTES_IN_MEGABYTE = 1000000;
	
	public String debugLine = "";
	
	public OpenGLOutputRenderer() {
		try {
			logger = Logger.getLogger("OpenGLOutputRenderer");
			debugLinelogger = Logger.getLogger("DebugLine");
			window = new Window("Bosstrove's Revenge", 0, 0);
			inputHandler = new GlfwInputHandler();
			
			lastRenderTime = System.currentTimeMillis();
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
					logger.log(Level.INFO, "Using Consolas® as font.");
					font = consolas;
				}
			}
			fontManager = new StbFontManager(font);
			fontManager.init();
		} catch (Exception e) {
			e.printStackTrace();
			BosstrovesRevenge.get().shutdown(-1);
		}
		
		model = new PixelModel();
		
		glfwSetKeyCallback(window.getHandle(), (long windowHandle, int key, int scancode, int action, int mods) -> {
			try {inputHandler.handleInput(key);} catch (Throwable e) {}
		});
	}

	public void render(PixelAndTextGrid pg) {
		//if(pg != null) {
			if(!(System.getProperty(Relauncher.FORCE_NORENDER) != null && System.getProperty(Relauncher.FORCE_NORENDER).equals("true"))) {
				
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
				
				if (window.isResized()) {
					glViewport(0, 0, window.getWidth(), window.getHeight());
					window.setResized(false);
				}
				
				glUseProgram(shader.getHandle());
				glUniform1i(glGetUniformLocation(shader.getHandle(), "texture1"), 0);
				
				try (MemoryStack stack = MemoryStack.stackPush()) {
					IntBuffer widthBuf = stack.mallocInt(1);
					IntBuffer heightBuf = stack.mallocInt(1);
					glfwGetWindowSize(window.getHandle(), widthBuf, heightBuf);
					int width = widthBuf.get();
					int height = heightBuf.get();
					float aspectRatio;
					float pixelSize;
					/*
					           r a t i o * 2                        2 
					    +- - - - - - - - - - - - -         +- - - - - - - - -
					    |                                  |
					    |                                r |
					    |                                a |
					  2 |                                t |
					    |                                i |
					    |                                o |
					    |                                * |    
					                                     2 | 
					                                       |
					                                       |
					*/
					if(width > height) {
						aspectRatio = (float)width / (float)height;
						pixelSize = 2F / (float)StaticDefaults.OUTPUT_HEIGHT;
						shader.setMatrix4f("projection", new Matrix4f().ortho(-aspectRatio, aspectRatio, -1, 1, 0, 1));
					} else {
						aspectRatio = (float)height / (float)width;
						pixelSize = 2F / (float)StaticDefaults.OUTPUT_WIDTH;
						shader.setMatrix4f("projection", new Matrix4f().ortho(-1, 1, -aspectRatio, aspectRatio, 0, 1));
					}
					Matrix4f scaledModelMatrix = new Matrix4f().scale(pixelSize, pixelSize, 1F);
					
					for (int rowNum = 0; rowNum < (pg.getHeight() / 2) * 2; rowNum += 2) {
						ArrayList<Color> row1 = pg.getRow(rowNum);
						ArrayList<Color> row2 = pg.getRow(rowNum + 1);
						ArrayList<Character> rowTxt = pg.getCharacterRow(rowNum);
						
						for (int colNum = 0; colNum < pg.getWidth(); colNum++) {
							
							if(rowTxt.get(colNum) == StaticDefaults.RESET_CHAR) {
									
								if(!row1.get(colNum).equals(StaticDefaults.RESET_COLOR)) {
									shader.setMatrix4f("model", new Matrix4f(scaledModelMatrix).translate(
											(-pg.getWidth()/2F + colNum),
											(pg.getHeight()/2F - rowNum),
											0
									).scale(0.5F));
									shader.setVector4f("color", rgbToVector(
											TextureUtils.interpolate(StaticDefaults.RESET_COLOR, row1.get(colNum), row1.get(colNum).getAlpha() / 255D)));
									model.render();
								}
								if(!row2.get(colNum).equals(StaticDefaults.RESET_COLOR)) {
									shader.setMatrix4f("model", new Matrix4f(scaledModelMatrix).translate(
											(-pg.getWidth()/2F + colNum),
											(pg.getHeight()/2F - rowNum - 1),
											0
									).scale(0.5F));
									shader.setVector4f("color", rgbToVector(
											TextureUtils.interpolate(StaticDefaults.RESET_COLOR, row2.get(colNum), row2.get(colNum).getAlpha() / 255D)));
									model.render();
								}
							} else {
								shader.setBool("useTexture", true);
								shader.setMatrix4f("model", new Matrix4f(scaledModelMatrix).translate(
										(-pg.getWidth()/2F + colNum - 0.5F),
										(pg.getHeight()/2F - rowNum - 1.0F),
										0
								).scale(1F, -1F, 1F));
								Pair<Color, Color> colors = pg.getColorsAt(colNum, rowNum);
								shader.setVector4f("color", rgbToVector(
										TextureUtils.interpolate(StaticDefaults.RESET_COLOR, colors.getFirst(), colors.getFirst().getAlpha() / 255D)));
								fontManager.renderCharacter(rowTxt.get(colNum));
								shader.setBool("useTexture", false);
								
								shader.setMatrix4f("model", new Matrix4f(scaledModelMatrix).translate(
										(-pg.getWidth()/2F + colNum),
										(pg.getHeight()/2F - rowNum - 1F/2F),
										0
								).scale(0.5F, 1F, 1F));
								shader.setVector4f("color", rgbToVector(
										TextureUtils.interpolate(StaticDefaults.RESET_COLOR, colors.getSecond(), colors.getSecond().getAlpha() / 255D)));
								model.render();
								
							}
							
						}
					}
				}
				
				//RENDER!
				glfwSwapBuffers(window.getHandle());
				glfwPollEvents();
				
				if(window.windowShouldClose()) {
					BosstrovesRevenge.get().shutdown(0);
				}
				
				//FPS Benchmarking
				long currTime = System.currentTimeMillis();
				fps = 1000D / (currTime - lastRenderTime);
				lastRenderTime = currTime;
				//System.out.printf("FPS: %.2f\n", fps);
			}
		//}
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
	
	public void setDebugLine(String s) {
		debugLine = s;
		debugLinelogger.log(Level.INFO, s);
	}

	public void displayFatalError(Throwable e) {
		error = e;
		
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
		
	}

}