package ai.arcblroth.boss.util;

public class Vector2D extends Pair<Double, Double> {

	public Vector2D(Double first, Double second) {
		super(first, second);
	}
	
	public Vector2D add(Vector2D other) {
		return new Vector2D(this.getX() + other.getX(), this.getY() + other.getY());
	}
	
	public Vector2D subtract(Vector2D other) {
		return new Vector2D(this.getX() - other.getX(), this.getY() - other.getY());
	}
	
	public Vector2D multiply(double scalar) {
		return new Vector2D(this.getX() * scalar, this.getY() * scalar);
	}
	
	public double dot(Vector2D other) {
		return this.getX() * other.getX() + this.getY() * other.getY();
	}
	
	public double getX() {
		return super.getFirst();
	}
	
	public double getY() {
		return super.getSecond();
	}
	
}
