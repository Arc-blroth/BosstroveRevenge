package ai.arcblroth.boss.util;

import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.PixelGrid;

public class TextureUtils {
	
	public static Color interpolate(Color color1, Color color2, double between) {
		return interpolate(color1, color2, between, false);
	}
	
	public static Color interpolate(Color color1, Color color2, double between, boolean lerp) {
		double[] c1HSB = color1.getAsHSBA();
		double[] c2HSB = color2.getAsHSBA();
		
		double finalHue = c1HSB[0] + between * (c2HSB[0] - c1HSB[0]);
		
		//Color lerping
		if(lerp) {
			double delta = c2HSB[0] - c1HSB[0];
			if(delta > 0.5) {
				c1HSB[0] = c1HSB[0] + 1;
				finalHue = Math.abs((c1HSB[0] + between * (c2HSB[0] - c1HSB[0])) % 1);
			}
		}
		
		return Color.getFromHSBA(
				finalHue,
				c1HSB[1] + between * (c2HSB[1] - c1HSB[1]),
				c1HSB[2] + between * (c2HSB[2] - c1HSB[2]),
				c1HSB[3] + between * (c2HSB[3] - c1HSB[3]));
	}
	
	public static Color interpolateRGB(Color color1, Color color2, double between) {
		return new Color(
				(int)Math.round(color1.getRed() + between * (color2.getRed() - color1.getRed())),
				(int)Math.round(color1.getGreen() + between * (color2.getGreen() - color1.getGreen())),
				(int)Math.round(color1.getBlue() + between * (color2.getBlue() - color1.getBlue()))
		);
	}
	
	public static PixelGrid tintColor(PixelGrid in, Color tint) {
		//Don't modify original
		in = new PixelGrid(in);
		Color solidTint = new Color(tint.getRed(), tint.getGreen(), tint.getBlue());
		int tintAlpha = tint.getAlpha();
		for (int rowNum = 0; rowNum < in.getHeight(); rowNum++) {
			for (int colNum = 0; colNum < in.getWidth(); colNum++) {
				Color interpolated = interpolate(in.getPixel(colNum, rowNum), solidTint, tintAlpha/255D);
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
	
	public static PixelGrid tintColorRGB(PixelGrid in, Color tint) {
		//Don't modify original
		in = new PixelGrid(in);
		Color solidTint = new Color(tint.getRed(), tint.getGreen(), tint.getBlue());
		int tintAlpha = tint.getAlpha();
		for (int rowNum = 0; rowNum < in.getHeight(); rowNum++) {
			for (int colNum = 0; colNum < in.getWidth(); colNum++) {
				Color interpolated = interpolateRGB(in.getPixel(colNum, rowNum), solidTint, tintAlpha/255D);
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
		for(int y = yOffset; y < dest.getHeight(); y++) {
			for(int x = xOffset; x < dest.getWidth(); x++) {
				Color destPx = dest.getPixel(x - xOffset, y - yOffset);
				Color srcPx = src.getPixel(x - xOffset, y - yOffset);
				dest.setPixel(x, y, 
						TextureUtils.interpolate(
								destPx,
								srcPx,
								(destPx.getAlpha())/255D)
				);
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
