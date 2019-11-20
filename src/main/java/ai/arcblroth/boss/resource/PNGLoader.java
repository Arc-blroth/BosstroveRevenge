package ai.arcblroth.boss.resource;

import java.io.File;
import java.net.URISyntaxException;

import ai.arcblroth.boss.render.Texture;
import ar.com.hjg.pngj.PngReader;

public final class PNGLoader {

	public static Texture loadPNG(ResourceLocation resourceLocation) throws NullPointerException {
		try {
			PngReader pngr = new PngReader(resourceLocation.toFile());
			return null;
		} catch (NullPointerException e) {
			throw e;
		} catch (URISyntaxException e) {
			NullPointerException npe = new NullPointerException(
					"Could not locate PNG resource " + resourceLocation.toString());
			npe.initCause(e);
			throw npe;
		}
	}

}
