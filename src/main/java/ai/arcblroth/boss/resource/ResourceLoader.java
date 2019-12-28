package ai.arcblroth.boss.resource;

import java.io.*;

import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.Texture;
import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReader;

public class ResourceLoader {

	public static Texture loadPNG(Resource resource) throws NullPointerException {
		try {
			PngReader pngr = new PngReader(resource.resolve().openStream());
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
					"Could not read PNG resource " + resource.toString());
			npe.initCause(e);
			throw npe;
		}
	}
	
	public static String loadTextFile(Resource resource) throws IOException, NullPointerException {
		try(BufferedReader br = new BufferedReader(new InputStreamReader(resource.resolve().openStream()))) {
			StringBuilder sb = new StringBuilder();
			br.lines().forEachOrdered(line -> sb.append(line).append("\n"));
			return sb.toString();
		} catch(IOException e) {
			throw e;
		}
	}
	
}
