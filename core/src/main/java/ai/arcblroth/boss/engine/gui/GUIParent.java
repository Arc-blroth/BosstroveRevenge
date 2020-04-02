package ai.arcblroth.boss.engine.gui;

import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.render.PixelAndTextGrid;
import ai.arcblroth.boss.util.TextureUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

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
		if(currentFocus == comp) currentFocus = null;
		children.remove(comp);
	}
	
	@Override
	public void render(PixelAndTextGrid target) {
		int targetWidth = target.getWidth();
		int targetHeight = target.getHeight();
		PixelAndTextGrid guiTarget = TextureUtils.buildTransparentTextGrid(targetWidth, targetHeight);

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
					PixelAndTextGrid childTarget = TextureUtils.buildTransparentTextGrid(resolvedWidth, resolvedHeight);
					child.render(childTarget);
					TextureUtils.overlay(childTarget, guiTarget, resolvedX, resolvedY);
				}
			}
		}
		TextureUtils.overlay(guiTarget, target);
	}

	public final boolean contains(GUIComponent c) {
		if(c == null) return false;
		if(c == this) return false;
		for(GUIComponent child : children.keySet()) {
			if(c == child) return true;
		}
		for(GUIComponent child : children.keySet()) {
			if(child instanceof GUIParent) {
				boolean found = ((GUIParent) child).contains(c);
				if(found) return true;
			}
		}
		return false;
	}

	protected final List<GUIParent> findPathTo(GUIComponent c) {
		if(c == null) return null;
		if(!contains(c)) return null;
		LinkedList<GUIParent> path = new LinkedList<>();
		findPathTo(c, path);
		return path;
	}

	// Really bad algorithm, should improve later
	protected final void findPathTo(GUIComponent c, LinkedList<GUIParent> path) {
		for(GUIComponent child : children.keySet()) {
			if(c == child) {
				path.add(this);
				return;
			}
		}
		for(GUIComponent child : children.keySet()) {
			if(child instanceof GUIParent) {
				if(((GUIParent) child).contains(c)) {
					path.add(this);
					((GUIParent) child).findPathTo(c, path);
					return;
				}
			}
		}
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
			if(children.containsKey(newFocus)) {
				currentFocus = newFocus;
			} else {
				throw new IllegalArgumentException("focused component must be a direct child of this component");
			}
		}
	}
	
	public void setFocusedComponentRecursively(GUIComponent newFocus) {
		if(newFocus == null) {
			currentFocus = null;
		} else if(newFocus == this) {
			throw new IllegalArgumentException("cannot set this component's focus to itself");
		} else {
			if(children.containsKey(newFocus)) {
				currentFocus = newFocus;
			} else {
				List<GUIParent> path = findPathTo(newFocus);
				if(path != null) {
					path.forEach(pathComponent -> pathComponent.setFocusedComponentUnsafe(newFocus));
				} else {
					throw new IllegalArgumentException("focused component must be a child of this component or its subcomponents");
				}
			}
		}
	}

	private void setFocusedComponentUnsafe(GUIComponent newFocus) {
		currentFocus = newFocus;
	}
	
	@Override
	public void onInput(Keybind k) {
		if(currentFocus != null) {
			currentFocus.onInput(k);
		}
	}
	
}
