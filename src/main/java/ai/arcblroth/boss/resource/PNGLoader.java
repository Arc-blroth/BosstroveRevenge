package ai.arcblroth.boss.resource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.render.Color;
import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReader;

public final class PNGLoader {

	public static Texture loadPNG(ResourceLocation resourceLocation) throws NullPointerException {
		try {
			PngReader pngr = new PngReader(resourceLocation.resolve().openStream());
			Texture t = new Texture(pngr.imgInfo.cols, pngr.imgInfo.rows);
			int channels = pngr.imgInfo.channels;
			for (int row = 0; row < pngr.imgInfo.rows; row++) {
				IImageLine ill = pngr.readRow();
				int[] scanline = ((ImageLineInt) ill).getScanline();
				for (int col = 0; col < pngr.imgInfo.cols; col++) {
					if(pngr.imgInfo.alpha) {
						t.setPixel(col, row, new Color(
								scanline[col * channels],
								scanline[col * channels + 1],
								scanline[col * channels + 2],
								scanline[col * channels + 3]));
					} else {
						t.setPixel(col, row, new Color(
								scanline[col * channels],
								scanline[col * channels + 1],
								scanline[col * channels + 2]));
					}
				}
			}
			pngr.end();
			return t;
		} catch (NullPointerException e) {
			throw e;
		}  catch (IOException e) {
			NullPointerException npe = new NullPointerException(
					"Could not read PNG resource " + resourceLocation.toString());
			npe.initCause(e);
			throw npe;
		}
	}

}
