package ai.arcblroth.boss.engine.gui;

import java.util.HashMap;
import java.util.TreeMap;

import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.PixelAndTextGrid;
import ai.arcblroth.boss.util.StaticDefaults;
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
		PixelAndTextGrid guiTarget = buildTransparentGrid(targetWidth, targetHeight);

		TreeMap<Integer, GUIComponent> sortedChildren = new TreeMap<>();
		children.forEach((comp, constraints) -> {
			if (!sortedChildren.containsKey(constraints.getZOrder())) {
				sortedChildren.put(constraints.getZOrder(), comp);
			} else {
				sortedChildren.put(constraints.getZOrder() + 1, comp);
			}
		});
		while (!sortedChildren.isEmpty()) {
			GUIComponent child = sortedChildren.pollFirstEntry().getValue();
			if(!child.isHidden()) {
				GUIConstraints constraints = children.get(child);
				int resolvedX = constraints.resolveX(targetWidth, targetHeight);
				int resolvedY = constraints.resolveY(targetWidth, targetHeight);
				int resolvedWidth = Math.min(constraints.resolveWidth(targetWidth, targetHeight), targetWidth - resolvedX);
				int resolvedHeight = Math.min(constraints.resolveHeight(targetWidth, targetHeight), targetHeight - resolvedY);
				if (resolvedX < targetWidth && resolvedY < targetHeight && resolvedWidth > 0 && resolvedHeight > 0) {
					PixelAndTextGrid childTarget = buildTransparentGrid(resolvedWidth, resolvedHeight);
					child.render(childTarget);
					TextureUtils.overlay(childTarget, guiTarget, resolvedX, resolvedY);
				}
			}
		}
		TextureUtils.overlay(guiTarget, target);
	}

	protected static final PixelAndTextGrid buildTransparentGrid(int width, int height) {
		PixelAndTextGrid grid = new PixelAndTextGrid(width, height);
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				grid.set(x, y, Color.TRANSPARENT);
			}
		}
		return grid;
	}
	
	public GUIComponent getFocusedComponent() {
		return currentFocus;
	}
	
	public void setFocusedComponent(GUIComponent newFocus) {
		if(newFocus == null) {
			currentFocus = null;
		} else if(newFocus == this) {
			throw new IllegalArgumentException("cannot set this component's focus to itself");
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
