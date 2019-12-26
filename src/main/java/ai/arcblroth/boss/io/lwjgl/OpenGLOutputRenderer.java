package ai.arcblroth.boss.io.lwjgl;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.Relauncher;
import ai.arcblroth.boss.io.IOutputRenderer;
import ai.arcblroth.boss.render.*;
import ai.arcblroth.boss.resource.Resource;
import ai.arcblroth.boss.util.StaticDefaults;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import static ai.arcblroth.boss.io.lwjgl.OpenGLUtils.*;

import java.nio.IntBuffer;
import java.util.ArrayList;

public class OpenGLOutputRenderer implements IOutputRenderer {
	
	private Throwable error;
	private Window window;
	private Shader shader;
	private PixelModel model;
	
	private static final boolean SHOW_FPS = true;
	private double fps = 1;
	private long lastRenderTime;
	private static final long BYTES_IN_MEGABYTE = 1000000;
	
	public String debugLine = "";
	
	public OpenGLOutputRenderer() {
		try {
			window = new Window("Bosstrove's Revenge", StaticDefaults.OUTPUT_WIDTH * StaticDefaults.CHARACTER_WIDTH, StaticDefaults.OUTPUT_HEIGHT / 2 * StaticDefaults.CHARACTER_HEIGHT);
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
			shader = new Shader(new Resource("shader/pixel.vert"), new Resource("shader/pixel.frag"));
		} catch (Exception e) {
			e.printStackTrace();
			BosstrovesRevenge.get().shutdown();
		}
		
		model = new PixelModel();
		
		glfwSetKeyCallback(window.getHandle(), (long windowHandle, int key, int scancode, int action, int mods) -> {
			if(windowHandle != window.getHandle()) return;
			
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
				try (MemoryStack stack = MemoryStack.stackPush()) {
					IntBuffer widthBuf = stack.mallocInt(1);
					IntBuffer heightBuf = stack.mallocInt(1);
					glfwGetWindowSize(window.getHandle(), widthBuf, heightBuf);
					int width = widthBuf.get();
					int height = heightBuf.get();
					float aspectRatio;
					float pixelSize;
					if(width > height) {
						aspectRatio = (float)width / (float)height;
						pixelSize = aspectRatio * 2F / (float)StaticDefaults.OUTPUT_WIDTH;
						shader.setMatrix4f("projection", new Matrix4f().ortho(-aspectRatio, aspectRatio, -1, 1, 0, 1));
					} else {
						aspectRatio = (float)height / (float)width;
						pixelSize = aspectRatio * 2F / (float)StaticDefaults.OUTPUT_HEIGHT;
						shader.setMatrix4f("projection", new Matrix4f().ortho(-1, 1, -aspectRatio, aspectRatio, 0, 1));
					}
					Matrix4f scaledModelMatrix = new Matrix4f().scale(pixelSize, pixelSize, 1F);
					
					for (int rowNum = 0; rowNum < (pg.getHeight() / 2) * 2; rowNum += 2) {
						ArrayList<Color> row1 = pg.getRow(rowNum);
						ArrayList<Color> row2 = pg.getRow(rowNum + 1);
						ArrayList<Character> rowTxt = pg.getCharacterRow(rowNum);
						
						for (int colNum = 0; colNum < pg.getWidth(); colNum++) {
							
							if(rowTxt.get(colNum) == StaticDefaults.RESET_CHAR) {
								
								shader.setMatrix4f("model", new Matrix4f(scaledModelMatrix).translate(
										(-pg.getWidth()/2F + colNum),
										(pg.getHeight()/2F - rowNum),
										0
								).scale(0.5F));
								shader.setVector3f("color", rgbToVector(row1.get(colNum)));
								model.render();
								shader.setMatrix4f("model", new Matrix4f(scaledModelMatrix).translate(
										(-pg.getWidth()/2F + colNum),
										(pg.getHeight()/2F - rowNum - 1),
										0
								).scale(0.5F));
								shader.setVector3f("color", rgbToVector(row2.get(colNum)));
								model.render();
							} else {
								
							}
							
						}
					}
					model.render();
				}
				
				//RENDER!
				glfwSwapBuffers(window.getHandle());
				glfwPollEvents();
				
				if(window.windowShouldClose()) {
					BosstrovesRevenge.get().shutdown();
				}
				
				//FPS Benchmarking
				long currTime = System.currentTimeMillis();
				fps = 1000D / (currTime - lastRenderTime);
				lastRenderTime = currTime;
			}
		//}
	}
	
	@Override
	public void dispose() {
		model.dispose();
		glfwMakeContextCurrent(NULL);
		glfwDestroyWindow(window.getHandle());
		glfwSetErrorCallback(null).free();
		glfwTerminate();
	}
	
	public void setDebugLine(String s) {
		debugLine = s;
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