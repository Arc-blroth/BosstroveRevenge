package ai.arcblroth.boss.io.lwjgl;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL30.*;

import java.nio.IntBuffer;
import java.util.Arrays;

import org.joml.Matrix4f;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.system.MemoryStack;

public class CharacterModel {
	
	private static final int SIZEOF_FLOAT = 4;
	private static final int indices[] = {
			0, 1, 3,
			1, 2, 3
	};
	
	private final int VAO;
	private final int VBO;
	private final int EBO;
	
	public CharacterModel(STBTTAlignedQuad quad) {
		float[] verticesAndTextureCoords = new float[] {
				//   ----Vertices----         ----TexCoords----
				quad.x0(), quad.y0(), 0,    quad.s0(), quad.t0(),
				quad.x1(), quad.y0(), 0,    quad.s1(), quad.t0(),
				quad.x1(), quad.y1(), 0,    quad.s1(), quad.t1(),
				quad.x0(), quad.y1(), 0,    quad.s0(), quad.t1()
		};
		
		VAO = glGenVertexArrays();
		glBindVertexArray(VAO);

		VBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, VBO);
		glBufferData(GL_ARRAY_BUFFER, verticesAndTextureCoords, GL_STATIC_DRAW);
		
		EBO = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

		glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * SIZEOF_FLOAT, 0);
		glEnableVertexAttribArray(0);

		glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * SIZEOF_FLOAT, 3 * SIZEOF_FLOAT);
		glEnableVertexAttribArray(1);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
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
	}

}
