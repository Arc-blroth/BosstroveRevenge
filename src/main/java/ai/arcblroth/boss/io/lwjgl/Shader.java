package ai.arcblroth.boss.io.lwjgl;

import static org.lwjgl.opengl.GL20.*;

import java.io.IOException;
import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import ai.arcblroth.boss.resource.Resource;
import ai.arcblroth.boss.resource.TextLoader;

public class Shader {
	
	private int shaderProgramHandle;

	public Shader(Resource vertShaderPath, Resource fragShaderPath) throws IOException, NullPointerException, IllegalStateException {
		String vertShaderSrc = TextLoader.loadTextFile(vertShaderPath);
		String fragShaderSrc = TextLoader.loadTextFile(fragShaderPath);
		
		int vertexShader = glCreateShader(GL_VERTEX_SHADER);
		int fragShader = glCreateShader(GL_FRAGMENT_SHADER);
		
		//Compile
		glShaderSource(vertexShader, vertShaderSrc);
		glCompileShader(vertexShader);
		
		int shader_compiled = glGetShaderi(vertexShader, GL_COMPILE_STATUS);
		if(shader_compiled != GL_TRUE) {
			throw new IllegalStateException(
					"Could not compile shader " + vertShaderPath.toString() + ": " +
					glGetShaderInfoLog(vertexShader, glGetShaderi(vertexShader, GL_INFO_LOG_LENGTH)));
		}
		
		glShaderSource(fragShader, fragShaderSrc);
		glCompileShader(fragShader);
		
		shader_compiled = glGetShaderi(fragShader, GL_COMPILE_STATUS);
		if(shader_compiled != GL_TRUE) {
			throw new IllegalStateException(
					"Could not compile shader " + fragShaderPath.toString() + ": " +
					glGetShaderInfoLog(fragShader, glGetShaderi(fragShader, GL_INFO_LOG_LENGTH)));
		}
		
		//Link!
		shaderProgramHandle = glCreateProgram();
		glAttachShader(shaderProgramHandle, vertexShader);
		glAttachShader(shaderProgramHandle, fragShader);
		glLinkProgram(shaderProgramHandle);
		
		int program_compiled = glGetProgrami(shaderProgramHandle, GL_LINK_STATUS);
		if(program_compiled != GL_TRUE) {
			throw new IllegalStateException(
					"Could not link shader program: " + 
					glGetProgramInfoLog(shaderProgramHandle, glGetProgrami(shaderProgramHandle, GL_INFO_LOG_LENGTH)));
		}
		
		//Cleanup
		glDeleteShader(vertexShader);
		glDeleteShader(fragShader);
	}
	
	public void activate() {
		glUseProgram(shaderProgramHandle);
	}
	
	public int getHandle() {
		return shaderProgramHandle;
	}
	
	public void setVector3f(String name, Vector3f value) {
		glUniform3f(glGetUniformLocation(shaderProgramHandle, name), value.x, value.y, value.z);
	}

	public void setMatrix4f(String name, Matrix4f value) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer buffer = value.get(stack.mallocFloat(16));
			glUniformMatrix4fv(glGetUniformLocation(shaderProgramHandle, name), false, buffer);
		}
	}
	
}
