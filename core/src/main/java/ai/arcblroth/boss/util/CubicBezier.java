package ai.arcblroth.boss.util;

/**
 * Represents a parametric function based on a cubic beizer with control points
 * (0, 0) (1/3, y1) (2/3, y2) and (1, 1).
 */
public class CubicBezier {

	private double y1, y2;

	public CubicBezier(double y1, double y2) {
		this.y1 = y1;
		this.y2 = y2;
	}

	public double calculate(double t) {
		return 3 * Math.pow((1 - t), 2) * t * y1 + 3 * (1 - t) * Math.pow(t, 2) * y2 + Math.pow(t, 3);
	}

}
