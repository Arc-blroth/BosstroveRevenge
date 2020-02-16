package ai.arcblroth.boss.llama;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;

public class LlamaUtils {
	
	public static int round(double in) {
		return (int)Math.round(in);
	}
	
	public static Node getElementById(Parent p, String id) {
		ObservableList<Node> children = p.getChildrenUnmodifiable();
		for(int i = 0; i < children.size(); i++) {
			if(children.get(i).idProperty().get() != null) {
				if(children.get(i).idProperty().get().equals(id)) {
					return children.get(i);
				}
			}
		}
		for(int i = 0; i < children.size(); i++) {
			if(children.get(i) instanceof Parent) {
				Node n = getElementById((Parent) children.get(i), id);
				if(n != null) return n;
			}
		}
		return null;
	}
	
}
