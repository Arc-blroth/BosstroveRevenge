package ai.arcblroth.boss.io.lwjgl;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;

import ai.arcblroth.boss.resource.Resource;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBTruetype.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class StbFontManager {
	
	private static final int FIRST_CHAR_TO_BAKE = 32;
	private static final int LAST_CHAR_TO_BAKE = 255;
	private static final int BITMAP_WIDTH = 1024;
	private static final int BITMAP_HEIGHT = 1024;
	
	private final ByteBuffer font;
	
	private int texture;
	private STBTTBakedChar.Buffer characters;
	
	private Logger logger;
	private final HashMap<Character, CharacterModel> charizardCache = new HashMap<>(LAST_CHAR_TO_BAKE - FIRST_CHAR_TO_BAKE);
	private int advanceWidth, leftSideBearing;
	
	public StbFontManager(Resource fontLocation) throws NullPointerException, IOException {
		font = OpenGLUtils.loadAsByteBuffer(fontLocation);
		logger = Logger.getLogger("StbFontManager");
	}
	
	public void init() {
		//It's bakin' time!
		long bakinTime = System.currentTimeMillis();
		
        glEnable(GL_TEXTURE_2D);
		
		texture = glGenTextures();
		characters = STBTTBakedChar.malloc(LAST_CHAR_TO_BAKE - FIRST_CHAR_TO_BAKE + 1);
		
		ByteBuffer alphaBitmap = BufferUtils.createByteBuffer(BITMAP_WIDTH * BITMAP_HEIGHT);
        int baked = stbtt_BakeFontBitmap(font, OpenGLUtils.STB_FONT_CHARACTER_HEIGHT, alphaBitmap, BITMAP_WIDTH, BITMAP_HEIGHT, FIRST_CHAR_TO_BAKE, characters);
        logger.log(Level.INFO, "Font baking result: " + baked);
       
        // stb bakes a bitmap of only GL_ALPHAs. But GL_ALPHA is deprecated, thus we
        // must manually convert the bitmap to GL_RGBA.
        byte[] alphaBytes = new byte[alphaBitmap.remaining()];
        alphaBitmap.get(alphaBytes);
       
        ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_WIDTH * BITMAP_HEIGHT * 4);
        final byte twofiftyfive = (byte) 255;
        for(byte alpha : alphaBytes) {
        	bitmap.put(twofiftyfive);
        	bitmap.put(twofiftyfive);
        	bitmap.put(twofiftyfive);
        	bitmap.put(alpha);
        }
        bitmap.flip();
        
        //Bind and upload texture
        glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, texture);
	    glPixelStorei(GL_PACK_ALIGNMENT, 1);
	    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, BITMAP_WIDTH, BITMAP_HEIGHT, 0, GL_RGBA, GL_UNSIGNED_BYTE, bitmap);
        glGenerateMipmap(GL_TEXTURE_2D);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        
        //Unbind
        glBindTexture(GL_TEXTURE_2D, 0);
        
        //Get font metrics.
        //Since this is guaranteed to be a monospace font, we only need one letter's metrics.
        STBTTFontinfo info = STBTTFontinfo.create();
		if (stbtt_InitFont(info, font)) {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				IntBuffer advanceWidthBuf = stack.mallocInt(1);
				IntBuffer leftSideBearingBuf = stack.mallocInt(1);

				stbtt_GetCodepointHMetrics(info, 'M', advanceWidthBuf, leftSideBearingBuf);

				advanceWidth = advanceWidthBuf.get(0) / OpenGLUtils.STB_FONT_CHARACTER_WIDTH;
				leftSideBearing = leftSideBearingBuf.get(0) / OpenGLUtils.STB_FONT_CHARACTER_WIDTH;
			}
		} else {
			throw new RuntimeException("Could not load stb font info.");
		}
        
		logger.log(Level.INFO, "Baked font texture in " + (System.currentTimeMillis() - bakinTime) + "ms!");
	}
	
	public void renderCharacter(char c) {
        glEnable(GL_TEXTURE_2D);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture);
        
        if(!charizardCache.containsKey(c)) {
    		logger.log(Level.FINER, "Generating model for character '" + c + "'");
	        try (MemoryStack stack = MemoryStack.stackPush()) {
	        	STBTTAlignedQuad quad = STBTTAlignedQuad.mallocStack(stack);
	        	FloatBuffer x = stack.mallocFloat(1);
	        	FloatBuffer y = stack.mallocFloat(1);
	        	stbtt_GetBakedQuad(characters,
	        			BITMAP_WIDTH, BITMAP_HEIGHT,
	        			c - FIRST_CHAR_TO_BAKE, x, y, quad, true);
	        	CharacterModel model = new CharacterModel(quad, advanceWidth);
	        	charizardCache.put(c, model);
	        }
		}
        charizardCache.get(c).render();

        glBindTexture(GL_TEXTURE_2D, 0);
	}
	
	public void dispose() {
		charizardCache.forEach((charizard, model) -> model.dispose());
		characters.free();
	}
	
}
