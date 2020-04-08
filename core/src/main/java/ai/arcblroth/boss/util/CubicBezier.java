package ai.arcblroth.boss.util;

public class CubicBezier {

	private double u1, u2;

	public CubicBezier(double u1, double u2) {
		this.u1 = u1;
		this.u2 = u2;
	}

	public double calculate(double t) {
		return 3 * Math.pow((1 - t), 2) * t * u1 + 3 * (1 - t) * Math.pow(t, 2) * u2 + Math.pow(t, 3);
	}

}
