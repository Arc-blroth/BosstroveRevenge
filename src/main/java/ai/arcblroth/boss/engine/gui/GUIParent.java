package ai.arcblroth.boss.engine.gui;

import java.util.HashMap;
import java.util.TreeMap;

import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.PixelAndTextGrid;
import ai.arcblroth.boss.util.TextureUtils;

public abstract class GUIParent extends GUIComponent {
	
	private HashMap<GUIComponent, GUIConstraints> children;
	private GUIComponent currentFocus;
	
	public GUIParent() {
		children = new HashMap<>();
	}
	
	public void add(GUIComponent comp, GUIConstraints constraints) {
		children.put(comp, constraints);
	}

	public void remove(GUIComponent comp) {
		children.remove(comp);
	}
	
	@Override
	public void render(PixelAndTextGrid target) {
		int targetWidth = target.getWidth();
		int targetHeight = target.getHeight();
		
		TreeMap<Integer, GUIComponent> sortedChildren = new TreeMap<>();
		children.forEach((comp, constraints) -> {
			sortedChildren.put(constraints.getZOrder(), comp);
		});
		while(!sortedChildren.isEmpty()) {
			GUIComponent child = sortedChildren.pollFirstEntry().getValue();
			GUIConstraints constraints = children.get(child);
			int resolvedX = constraints.resolveX(targetWidth, targetHeight);
			int resolvedY = constraints.resolveY(targetWidth, targetHeight);
			int resolvedWidth = constraints.resolveWidth(targetWidth, targetHeight);
			int resolvedHeight = constraints.resolveHeight(targetWidth, targetHeight);
			if(resolvedX < target.getWidth() && resolvedY < target.getHeight()
					&& resolvedWidth > 0 && resolvedHeight > 0) {
				PixelAndTextGrid childTarget = new PixelAndTextGrid(resolvedWidth, resolvedHeight);
				childTarget.forEach(((x, y, color) -> childTarget.set(x, y, Color.TRANSPARENT)));
				child.render(childTarget);
				TextureUtils.overlay(childTarget, target, resolvedX, resolvedY);
			}
		}
	}
	
	public GUIComponent getFocusedComponent() {
		return currentFocus;
	}
	
	public void setFocusedComponent(GUIComponent newFocus) {
		if(newFocus == null) {
			currentFocus = null;
		} else {
			if(!children.containsKey(newFocus)) throw new IllegalArgumentException("focused component must be a child of this component");
			currentFocus = newFocus;
		}
	}
	
	@Override
	public void onInput(Character c) {
		if(currentFocus != null) {
			currentFocus.onInput(c);
		}
	}
	
}
