package ai.arcblroth.boss.io.lwjgl;

import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.opengl.GL30.*;

import java.nio.IntBuffer;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

public class PixelModel {

	private static final float vertices[] = {
			 1.0f,  1.0f,  0.0f, // top right
			 1.0f, -1.0f,  0.0f, // bottom right
			-1.0f, -1.0f,  0.0f, // bottom left
			-1.0f,  1.0f,  0.0f  // top left
	};
	private static final int indices[] = {
			0, 1, 3,
			1, 2, 3
	};
	private final int VAO;
	private final int VBO;
	private final int EBO;

	public PixelModel() {

		VAO = glGenVertexArrays();
		glBindVertexArray(VAO);

		VBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, VBO);
		glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
		
		EBO = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glEnableVertexAttribArray(0);
		glBindVertexArray(0);
	}

	public void render() {
		glBindVertexArray(VAO);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
		glBindVertexArray(0);
	}
	
	public void dispose() {
		glDeleteVertexArrays(VAO);
		glDeleteBuffers(VBO);
		glDeleteBuffers(EBO);
	}

}
