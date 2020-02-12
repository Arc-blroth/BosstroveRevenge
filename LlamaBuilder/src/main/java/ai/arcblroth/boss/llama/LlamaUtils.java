package ai.arcblroth.boss.llama;

import java.awt.Dimension;
import java.awt.Rectangle;

public class LlamaUtils {
	
	public static int round(double in) {
		return (int)Math.round(in);
	}
	
	public static Dimension roundDimension(double x, double y) {
		return new Dimension((int)Math.round(x), (int)Math.round(y));
	}
	
	public static Rectangle roundRectangle(double x, double y, double width, double height) {
		return new Rectangle((int)Math.round(x), (int)Math.round(y), (int)Math.round(width), (int)Math.round(height));
	}
	
}
