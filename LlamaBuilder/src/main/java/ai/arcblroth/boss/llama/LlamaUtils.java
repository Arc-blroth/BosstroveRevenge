package ai.arcblroth.boss.llama;

import ai.arcblroth.boss.render.Color;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.paint.Paint;

public class LlamaUtils {
	
	public static int round(double in) {
		return (int)Math.round(in);
	}
	
	public static Node getElementById(Parent p, String id) {
		return p.lookup("#" + id);
	}

	public static Paint colorToPaint(Color color) {
		return new javafx.scene.paint.Color(color.getRed()/255D, color.getGreen()/255D, color.getBlue()/255D, color.getAlpha()/255D);
	}
	
}
