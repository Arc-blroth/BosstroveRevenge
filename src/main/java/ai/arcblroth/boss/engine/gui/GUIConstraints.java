package ai.arcblroth.boss.engine.gui;

public class GUIConstraints {
	
	private double xFactor, yFactor, wFactor, hFactor;
	private int xOffset, yOffset, wOffset, hOffset;
	private int zOrder;
	
	/**
	 * Constructs a new GUIConstraints object from a formatted string.
	 * If a parameter ends in "px" or has no ending, that constraint is assumed to be absolute.
	 * If a parameter ends in "%", that constraint is assumed to be relative.
	 * Absolute constraints must be integers. Relative constraints are doubles.
	 * 
	 * @param x x string
	 * @param y y string
	 * @param w width string
	 * @param h height string
	 * @param zOrder z order
	 */
	public GUIConstraints(String x, String y, String w, String h, int zOrder) {
		if(x == null || y == null || w == null || h == null) throw new NullPointerException();
		
		if(x.endsWith("%")) {
			xFactor = Double.parseDouble(x.substring(0, x.length() - 1)) / 100D;
			xOffset = 0;
		} else if(x.endsWith("px")) {
			xFactor = 0;
			xOffset = Integer.parseInt(x.substring(0, x.length() - 2));
		} else {
			xFactor = 0;
			xOffset = Integer.parseInt(x);
		}
		
		if(y.endsWith("%")) {
			yFactor = Double.parseDouble(y.substring(0, y.length() - 1)) / 100D;
			yOffset = 0;
		} else if(y.endsWith("px")) {
			yFactor = 0;
			yOffset = Integer.parseInt(y.substring(0, y.length() - 2));
		} else {
			yFactor = 0;
			yOffset = Integer.parseInt(y);
		}
		
		if(w.endsWith("%")) {
			wFactor = Double.parseDouble(w.substring(0, w.length() - 1)) / 100D;
			wOffset = 0;
		} else if(w.endsWith("px")) {
			wFactor = 0;
			wOffset = Integer.parseInt(w.substring(0, w.length() - 2));
		} else {
			wFactor = 0;
			wOffset = Integer.parseInt(w);
		}
		
		if(h.endsWith("%")) {
			hFactor = Double.parseDouble(h.substring(0, h.length() - 1)) / 100D;
			hOffset = 0;
		} else if(h.endsWith("px")) {
			hFactor = 0;
			hOffset = Integer.parseInt(h.substring(0, h.length() - 2));
		} else {
			hFactor = 0;
			hOffset = Integer.parseInt(h);
		}
		
		this.zOrder = zOrder;
	}
	
	/**
	 * Constructs a new GUIConstraints object from relative factors and absolute offsets.
	 * The final value of each constraint is determined by the equation <tt>factor * width + offset</tt>.
	 * 
	 * @param xFactor relative x factor
	 * @param yFactor relative y factor
	 * @param wFactor relative width factor
	 * @param hFactor relative height factor
	 * @param xOffset absolute x offset
	 * @param yOffset absolute y offset
	 * @param wOffset absolute width offset
	 * @param hOffset absolute height offset
	 * @param zOrder  absolute z order
	 */
	public GUIConstraints(double xFactor, double yFactor, double wFactor, double hFactor, int xOffset, int yOffset, int wOffset, int hOffset, int zOrder) {
		this.xFactor = xFactor;
		this.yFactor = yFactor;
		this.wFactor = wFactor;
		this.hFactor = hFactor;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.wOffset = wOffset;
		this.hOffset = hOffset;
		this.zOrder  = zOrder ;
	}
		
	public int resolveX(int guiWidth, int guiHeight) {
		return (int)Math.round(xFactor * guiWidth + xOffset);
	}
	
	public int resolveY(int guiWidth, int guiHeight) {
		return (int)Math.round(yFactor * guiHeight + yOffset);
	}
	
	public int resolveWidth(int guiWidth, int guiHeight) {
		return (int)Math.round(wFactor * guiWidth + wOffset);
	}
	
	public int resolveHeight(int guiWidth, int guiHeight) {
		return (int)Math.round(wFactor * guiHeight + wOffset);
	}
	
	public int getZOrder() {
		return zOrder;
	}
	
}
