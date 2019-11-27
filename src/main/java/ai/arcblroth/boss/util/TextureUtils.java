package ai.arcblroth.boss.util;

import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.PixelGrid;

public class TextureUtils {
	
	public static Color interpolate(Color color1, Color color2, double between) {
		double[] c1HSB = color1.getAsHSBA();
		double[] c2HSB = color2.getAsHSBA();
		return Color.getFromHSBA(
				c1HSB[0] + between * (c2HSB[0] - c1HSB[0]),
				c1HSB[1] + between * (c2HSB[1] - c1HSB[1]),
				c1HSB[2] + between * (c2HSB[2] - c1HSB[2]),
				c1HSB[3] + between * (c2HSB[3] - c1HSB[3]));
	}
	
	public static PixelGrid tintColor(PixelGrid in, Color tint) {
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
	
}
