package ai.arcblroth.boss.io.lwjgl;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.resource.Resource;

public class OpenGLUtils {
	
	public static Vector4f rgbToVector(Color in) {
		return new Vector4f(in.getRed()/255F, in.getGreen()/255F, in.getBlue()/255F, in.getAlpha()/255F);
	}

	public static ByteBuffer loadAsByteBuffer(Resource resource) throws IOException, NullPointerException {
		BufferedInputStream bis = null;
		ByteArrayOutputStream bos = null;
		try {
			bis = new BufferedInputStream(resource.resolve().openStream());
			bos = new ByteArrayOutputStream();
			byte[] transferBuffer = new byte[4096];
			int read = 0;
	        while ((read = bis.read(transferBuffer)) != -1) {
	        	bos.write(transferBuffer, 0, read);
	        }
	        ByteBuffer buffer = BufferUtils.createByteBuffer(bos.size());
	        buffer.put(bos.toByteArray());
	        buffer.flip();
	        return buffer;
		} catch(IOException e) {
			throw e;
		} finally {
			if(bis != null) bis.close();
			if(bos != null) bos.close();
		}
	}
	
}
