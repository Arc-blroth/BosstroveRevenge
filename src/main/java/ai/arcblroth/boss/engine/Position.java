package ai.arcblroth.boss.engine;

import ai.arcblroth.boss.util.Pair;

public class Position extends Pair<Double, Double> {

	public Position(double x, double y) {
		super(x, y);
	}
	
	public double getX() {
		return getFirst();
	}
	
	public double getY() {
		return getSecond();
	}

}
