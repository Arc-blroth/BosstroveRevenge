package ai.arcblroth.boss.resource.load;

import ai.arcblroth.boss.render.MultiFrameTexture;
import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.resource.InternalResource;
import ai.arcblroth.boss.resource.Resource;

import java.util.TreeMap;

public class CharacterTextureLoader {

	public static final Resource FONT_ATLAS_LOCATION = new InternalResource("font/pressstar2p.btex");
	public static final char FIRST_CHAR_IN_ATLAS = 0x0020;
	public static final int TEXTURE_WIDTH = 8;
	public static final int TEXTURE_HEIGHT = 8;

	public static TreeMap<Character, Texture> loadFont(MultiFrameTextureLoader mftl) {
		MultiFrameTexture atlas = mftl.load(FONT_ATLAS_LOCATION);
		TreeMap<Character, Texture> out = new TreeMap<>();
		for(int i = 0; i < atlas.getFrames(); i++) {
			atlas.setCurrentFrame(i);
			out.put((char) (FIRST_CHAR_IN_ATLAS + i), new Texture(atlas));
		}
		return out;
	}

}
