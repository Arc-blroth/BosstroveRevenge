package ai.arcblroth.boss.render;

/**
 * A standalone Color class.
 * 
 * @author Arc'blroth
 */
public class Color {
	
	//These default color constants are taken from the java.awt.Color class.
    public static final Color WHITE = new Color(255, 255, 255);
    public static final Color LIGHT_GRAY = new Color(192, 192, 192);
    public static final Color GRAY = new Color(128, 128, 128);
    public static final Color DARK_GRAY = new Color(64, 64, 64);
    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color RED = new Color(255, 0, 0);
    public static final Color PINK = new Color(255, 175, 175);
    public static final Color ORANGE = new Color(255, 200, 0);
    public static final Color YELLOW = new Color(255, 255, 0);
    public static final Color GREEN = new Color(0, 255, 0);
    public static final Color MAGENTA = new Color(255, 0, 255);
    public static final Color CYAN = new Color(0, 255, 255);
    public static final Color BLUE = new Color(0, 0, 255);

    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    
	private final int colorValue;
	
	public Color(int r, int g, int b) {
		this(r, g, b, 255);
	}
	
	public Color(int r, int g, int b, int a) {
		r = clipRGBA(r);
		g = clipRGBA(g);
		b = clipRGBA(b);
		a = clipRGBA(a);
		colorValue = ((a & 0xFF) << 24) |
                	 ((r & 0xFF) << 16) |
                	 ((g & 0xFF) << 8)  |
                	 ((b & 0xFF) << 0);
	}
	
	public int getRed() {
        return (colorValue >> 16) & 0xFF;
    }
	
	public int getGreen() {
        return (colorValue >> 8) & 0xFF;
    }
	
    public int getBlue() {
        return (colorValue >> 0) & 0xFF;
    }
    
    public int getAlpha() {
        return (colorValue >> 24) & 0xFF;
    }
    
    public int getRGBA() {
        return colorValue;
    }
    
    public double[] getAsHSBA() {
    	double red = getRed();
    	double green = getGreen();
    	double blue = getBlue();
    	double h = 0, s = 0, b = 0;
    	double maxRGB = Math.max(red, Math.max(green, blue));
    	double minRGB = Math.min(red, Math.min(green, blue));
    	double delta = maxRGB - minRGB;
    	b = maxRGB;
    	if(maxRGB != 0) {
    		s = 255D * delta / maxRGB;
    		if(red == maxRGB) {
    			h = ((green - blue) / delta) % 6;
    		} else if(green == maxRGB) {
    			h = (blue - red) / delta + 2D;
    		} else if(blue == maxRGB) {
    			h = (red - green) / delta + 4D;
    		} else {
    			//idk how we would get here
    			throw new RuntimeException("The colors are off the charts!");
    		}	
    	} else {
    		s = 0;
    		h = -1;
    	}
    	h *= 60;
    	if(h < 0) h += 360;
    	if(new Double(h).isNaN()) h = 0;
    	return new double[] {
    			h / 360D,
    			s / 255D,
    			b / 255D,
    			getAlpha()};
    }
    
    public static Color getFromHSBA(double h, double s, double b) {
    	return getFromHSBA(h, s, b, 1D);
    }
    
    public static Color getFromHSBA(double h, double s, double b, double a) {
    	int red = 0, green = 0, blue = 0;
    	double newHue = h * 360D / 60D;
    	double newSat = s * 255D;
    	double newBri = b * 255D;
    	if(newSat == 0D) {
    		red = (int)Math.round(newBri);
    		green = (int)Math.round(newBri);
    		blue = (int)Math.round(newBri);
    	} else {
        	double maxRGB = newBri;
    		double delta = newSat * maxRGB / 255D;
    		if(newHue > 3) {
    			blue = (int)Math.round(maxRGB);
    			if(newHue > 4) {
    				green = (int)Math.round(maxRGB - delta);
    				red = (int)(Math.round((newHue - 4) * delta) + green);
    			} else {
    				red = (int)Math.round(maxRGB - delta);
    				green = (int)(red - Math.round((newHue - 4) * delta));
    			}
    		} else if(newHue > 1) {
    			green = (int)Math.round(maxRGB);
    			if(newHue > 2) {
    				red = (int)Math.round(maxRGB - delta);
    				blue = (int)(Math.round((newHue - 2) * delta) + red);
    			} else {
    				blue = (int)Math.round(maxRGB - delta);
    				red = (int)(blue - Math.round((newHue - 2) * delta));
    			}
    		} else if(newHue > -1) {
    			red = (int)Math.round(maxRGB);
    			if(newHue > 0) {
    				blue = (int)Math.round(maxRGB - delta);
    				green = (int)(Math.round(newHue * delta) + blue);
    			} else {
    				green = (int)Math.round(maxRGB - delta);
    				blue = (int)(green - Math.round(newHue * delta));
    			}
    		}
    	}
    	return new Color(red, green, blue, clipRGBA((int)Math.round(clipHSBA(a) * 255)));
    }
	
	private static int clipRGBA(int channelValue) {
		return Math.max(0, Math.min(channelValue, 255));
	}
	
	private static double clipHSBA(double channelValue) {
		return Math.max(0.0, Math.min(channelValue, 1.0));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + colorValue;
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
		Color other = (Color) obj;
		if (colorValue != other.colorValue)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("ai.arcblroth.boss.render.Color[r=%s, g=%s, b=%s, a=%s]", getRed(), getGreen(), getBlue(), getAlpha());
	}
	
}
