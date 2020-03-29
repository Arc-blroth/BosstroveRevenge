package ai.arcblroth.boss.util;

import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.PixelAndTextGrid;
import ai.arcblroth.boss.render.PixelGrid;

public class TextureUtils {

	public static Color interpolateRGB(Color color1, Color color2, double between) {
		return interpolateRGB(color1, color2, 1 - between, between);
	}

	public static Color interpolateRGB(Color color1, Color color2, double k1, double k2) {
		return new Color(
				(int)Math.round(color1.getRed() * k1 + (color2.getRed() * k2)),
				(int)Math.round(color1.getGreen() * k1 + (color2.getGreen() * k2)),
				(int)Math.round(color1.getBlue() * k1 + (color2.getBlue() * k2)),
				(int)Math.max(color1.getAlpha(), color2.getAlpha())
		);
	}

	public static Color interpolateHSB(Color color1, Color color2, double between) {
		return interpolateHSB(color1, color2, between, false);
	}

	public static Color interpolateHSB(Color color1, Color color2, double between, boolean lerp) {
		return interpolateHSB(color1, color2, 1 - between, between, lerp);
	}
	
	public static Color interpolateHSB(Color color1, Color color2, double k1, double k2, boolean lerp) {
		double[] c1HSB = color1.getAsHSBA();
		double[] c2HSB = color2.getAsHSBA();
		
		double finalHue = c1HSB[0] * k1 + c2HSB[0] * k2;
		
		//Color lerping
		if(lerp) {
			double delta = c2HSB[0] - c1HSB[0];
			if(delta > 0.5) {
				c1HSB[0] = c1HSB[0] + 1;
				finalHue = Math.abs((c1HSB[0] * k1 + c2HSB[0] * k2) % 1);
			}
		}
		
		return Color.getFromHSBA(
				finalHue,
				c1HSB[1] * k1 + c2HSB[1] * k2,
				c1HSB[2] * k1 + c2HSB[2] * k2,
				Math.max(c1HSB[3], c2HSB[3])
		);
	}
	
	public static Color invert(Color in) {
		return new Color(
			255 - in.getRed(),
			255 - in.getGreen(),
			255 - in.getBlue(),
			in.getAlpha()
		);
	}

	public static PixelGrid tintColor(PixelGrid in, Color tint) {
		return tintColor0(in, tint, TextureUtils::interpolateRGB);
	}
	
	public static PixelGrid tintColorHSB(PixelGrid in, Color tint) {
		return tintColor0(in, tint, TextureUtils::interpolateHSB);
	}

	private static PixelGrid tintColor0(PixelGrid in, Color tint, TriFunction<Color, Color, Double, Color> interFunction) {
		//Don't modify original
		in = new PixelGrid(in);
		Color solidTint = new Color(tint.getRed(), tint.getGreen(), tint.getBlue());
		int tintAlpha = tint.getAlpha();
		for (int rowNum = 0; rowNum < in.getHeight(); rowNum++) {
			for (int colNum = 0; colNum < in.getWidth(); colNum++) {
				Color interpolated = interFunction.apply(in.getPixel(colNum, rowNum), solidTint, tintAlpha/255D);
				in.setPixel(colNum, rowNum, new Color(
						interpolated.getRed(),
						interpolated.getGreen(),
						interpolated.getBlue(),
						in.getPixel(colNum, rowNum).getAlpha()
				));
			}
		}
		return in;
	}
	
	public static PixelGrid overlay(PixelGrid src, PixelGrid dest) {
		return overlay(src, dest, 0, 0);
	}
	
	public static PixelGrid overlay(PixelGrid src, PixelGrid dest, int xOffset, int yOffset) {
		return overlay0(src, dest, xOffset, yOffset, TextureUtils::interpolateRGB);
	}

	public static PixelGrid overlayHSB(PixelGrid src, PixelGrid dest) {
		return overlayHSB(src, dest, 0, 0);
	}

	public static PixelGrid overlayHSB(PixelGrid src, PixelGrid dest, int xOffset, int yOffset) {
		return overlay0(src, dest, xOffset, yOffset, TextureUtils::interpolateHSB);
	}

	private static PixelGrid overlay0(PixelGrid src, PixelGrid dest, int xOffset, int yOffset, TriFunction<Color, Color, Double, Color> interFunction) {
		for(int y = yOffset; y < dest.getHeight() && y - yOffset < src.getHeight(); y++) {
			for(int x = xOffset; x < dest.getWidth() && x - xOffset < src.getWidth(); x++) {
				Color destPx = dest.getPixel(x, y);
				Color srcPx = src.getPixel(x - xOffset, y - yOffset);
				dest.setPixel(x, y,
						interFunction.apply(
								destPx,
								srcPx,
								(srcPx.getAlpha())/255D)
				);
			}
		}
		return dest;
	}
	
	public static PixelGrid overlay(PixelAndTextGrid src, PixelAndTextGrid dest) {
		return overlay(src, dest, 0, 0);
	}
	
	public static PixelGrid overlay(PixelAndTextGrid src, PixelAndTextGrid dest, int xOffset, int yOffset) {
		return overlay0(src, dest, xOffset, yOffset, TextureUtils::interpolateRGB);
	}

	public static PixelGrid overlayHSB(PixelAndTextGrid src, PixelAndTextGrid dest) {
		return overlay(src, dest, 0, 0);
	}

	public static PixelGrid overlayHSB(PixelAndTextGrid src, PixelAndTextGrid dest, int xOffset, int yOffset) {
		return overlay0(src, dest, xOffset, yOffset, TextureUtils::interpolateHSB);
	}

	private static PixelGrid overlay0(PixelAndTextGrid src, PixelAndTextGrid dest, int xOffset, int yOffset, TriFunction<Color, Color, Double, Color> interFunction) {
		overlay0((PixelGrid)src, (PixelGrid)dest, xOffset, yOffset, interFunction);
		yOffset = yOffset / 2 * 2;
		for(int y = yOffset; y < dest.getHeight() / 2 * 2 && y - yOffset < src.getHeight() / 2 * 2; y += 2) {
			for(int x = xOffset; x < dest.getWidth() && x - xOffset < src.getWidth(); x++) {
				if(src.getCharacterAt(x - xOffset, y - yOffset) != StaticDefaults.RESET_CHAR) {
					Color srcForePx = src.getColorsAt(x - xOffset, y - yOffset).getFirst();
					Color srcBackPx = src.getColorsAt(x - xOffset, y - yOffset).getSecond();
					Color destForePx = dest.getColorsAt(x, y).getFirst();
					Color destBackPx = dest.getColorsAt(x, y).getSecond();
					dest.setCharacterAt(
							x, y,
							src.getCharacterAt(x - xOffset, y - yOffset),
							interFunction.apply(destBackPx, srcBackPx, (srcBackPx.getAlpha()) / 255D),
							interFunction.apply(destForePx, srcForePx, (srcForePx.getAlpha()) / 255D)
					);
				}
			}
		}
		return dest;
	}
	
	public static PixelGrid sub(PixelGrid src, int xOffset, int yOffset, int width, int height) {
		PixelGrid out = new PixelGrid(width, height);
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				out.setPixel(x, y, src.getPixel(x + xOffset, y + yOffset));
			}
		}
		return out;
	}
	
	public static PixelGrid flipX(PixelGrid src) {
		PixelGrid dest = new PixelGrid(src.getWidth(), src.getHeight());
		for(int y = 0; y < src.getHeight(); y++) {
			for(int x = 0; x < src.getWidth(); x++) {
				dest.setPixel(src.getWidth() - 1 - x, y, src.get(x, y));
			}
		}
		return dest;
	}
	
	public static PixelGrid flipY(PixelGrid src) {
		PixelGrid dest = new PixelGrid(src.getWidth(), src.getHeight());
		for(int y = 0; y < src.getHeight(); y++) {
			for(int x = 0; x < src.getWidth(); x++) {
				dest.setPixel(x, src.getHeight() - 1 - y, src.get(x, y));
			}
		}
		return dest;
	}
	
}
