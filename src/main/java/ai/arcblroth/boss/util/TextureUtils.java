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
	
	public static PixelGrid overlay(PixelGrid src, PixelGrid dest) {
		return overlay(src, dest, 0, 0);
	}
	
	public static PixelGrid overlay(PixelGrid src, PixelGrid dest, int xOffset, int yOffset) {
		for(int y = yOffset; y < dest.getHeight(); y++) {
			for(int x = xOffset; x < dest.getWidth(); x++) {
				dest.setPixel(x, y, src.getPixel(x - xOffset, y - yOffset));
			}
		}
		return dest;
	}
	
}
