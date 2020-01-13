package ai.arcblroth.boss.engine.hitbox;

import ai.arcblroth.boss.engine.Position;

public class Hitbox {
	
	private double x, y, width, height;
	
	public Hitbox(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public boolean intersects(Hitbox other) {
		 // If the left x-coordinate of the first hitbox is inside the other hitbox's x-range
		if ((this.x > other.x && this.x < other.x + other.width) || 
				(this.x + this.width > other.x && this.x + this.width < other.x + other.width)) {
			// If the top y-coordinate of the first hitbox is inside the other hitbox y-range
			if ((this.y > other.y && this.y < other.y + other.height) ||  
					(this.y + this.height > other.y && this.y + this.height < other.y + other.height)) {
				return true;
			}
		}
		return false;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(height);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(width);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Hitbox other = (Hitbox) obj;
		if (Double.doubleToLongBits(height) != Double.doubleToLongBits(other.height))
			return false;
		if (Double.doubleToLongBits(width) != Double.doubleToLongBits(other.width))
			return false;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Hitbox [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
	}
	
}
