package ai.arcblroth.boss.io.lwjgl;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_TRUE;
import static org.lwjgl.opengl.GL20.glClearColor;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private final String title;
    private int width;
    private int height;
    private long windowHandle;
    private boolean resized;
    private boolean fullscreen;
	private boolean hasBeenInit;

    public Window(String title, int width, int height) {
        this(title, width, height, false);
    }

    public Window(String title, int width, int height, boolean fullscreen) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.resized = false;
        this.fullscreen = fullscreen;
        this.hasBeenInit = false;
    }

    public void init() {
    	hasBeenInit = true;
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
		glfwWindowHint(GLFW_CENTER_CURSOR, GL_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        
        if(width == 0 && height == 0) {
            if(fullscreen) {
                width = vidmode.width();
                height = vidmode.height();
            } else {
                width = vidmode.width() / 2;
                height = vidmode.height() / 2;
            }
        }
        
        if(fullscreen) {
            windowHandle = glfwCreateWindow(width, height, title, glfwGetPrimaryMonitor(), NULL);
        } else {
            windowHandle = glfwCreateWindow(width, height, title, NULL, NULL);
        }

        if (windowHandle == NULL) {
            throw new RuntimeException("Failed to create window");
        }
        
        glfwSetFramebufferSizeCallback(windowHandle, (window, width, height) -> {
            this.width = width;
            this.height = height;
            this.setResized(true);
        });
        
        glfwSetWindowPos(
                windowHandle,
                (vidmode.width() - width) / 2,
                (vidmode.height() - height) / 2
        );

        glfwMakeContextCurrent(windowHandle);
        glfwSwapInterval(1);

        GL.createCapabilities();
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_TEXTURE_2D);
        
        glfwShowWindow(windowHandle);
        
        Logger.getLogger("Window").log(Level.INFO, "Created window 0x" + Long.toHexString(windowHandle));
    }

	public long getHandle() {
		return windowHandle;
	}

    public boolean isKeyPressed(int keyCode) {
        return glfwGetKey(windowHandle, keyCode) == GLFW_PRESS;
    }

    public boolean windowShouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }

    public String getTitle() {
        return title;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isResized() {
        return resized;
    }
    
    public boolean hasBeenInit() {
    	return hasBeenInit;
    }

    public void setResized(boolean resized) {
        this.resized = resized;
    }

    public void update() {
        glfwSwapBuffers(windowHandle);
        glfwPollEvents();
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if(this.fullscreen) {
            width = vidMode.width();
            height = vidMode.height();
            resized = true;
            glfwSetWindowMonitor(windowHandle, glfwGetPrimaryMonitor(), 0, 0, width, height, GLFW_DONT_CARE);
        } else {
            width = vidMode.width() / 2;
            height = vidMode.height() / 2;
            resized = true;
            glfwSetWindowMonitor(windowHandle, NULL, width / 2, height / 2, width, height, GLFW_DONT_CARE);
        }
    }

}
