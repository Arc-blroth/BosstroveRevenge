package ai.arcblroth.boss.engine.hitbox;

import java.util.LinkedList;

import ai.arcblroth.boss.engine.IHitboxed;

/**
 * A QuadTree implementation, based on that of
 * <a href="https://gamedevelopment.tutsplus.com/tutorials/quick-tip-use-quadtrees-to-detect-likely-collisions-in-2d-space--gamedev-374">
 * this tutorial</a>.
 */
public class QuadTree {
	
	private static final int MAX_OBJECTS = 256;
	private static final int MAX_DEPTH = 8;
	
	private int depth;
	private LinkedList<IHitboxed> objects;
	private QuadTree[] subnodes;
	private Hitbox bounds;
	
	public QuadTree(Hitbox bounds) {
		this(0, bounds);
	}
	
	private QuadTree(int depth, Hitbox bounds) {
		this.depth = depth;
		this.objects = new LinkedList<>();
		this.subnodes = new QuadTree[4];
		this.bounds = bounds;
	}
	
	/**
	 * Clears this QuadTree and any of its sub-trees.
	 */
	public void clear() {
		objects.clear();
		for (int i = 0; i < subnodes.length; i++) {
			if (subnodes[i] != null) {
				subnodes[i].clear();
				subnodes[i] = null;
			}
		}
	}
	
	/**
	 * Splits a QuadTree into 4 smaller trees.
	 */
	private void split() {
		int subWidth = (int) (bounds.getWidth() / 2);
		int subHeight = (int) (bounds.getHeight() / 2);
		int x = (int) bounds.getX();
		int y = (int) bounds.getY();

		subnodes[0] = new QuadTree(depth + 1, new Hitbox(x + subWidth, y, subWidth, subHeight));
		subnodes[1] = new QuadTree(depth + 1, new Hitbox(x, y, subWidth, subHeight));
		subnodes[2] = new QuadTree(depth + 1, new Hitbox(x, y + subHeight, subWidth, subHeight));
		subnodes[3] = new QuadTree(depth + 1, new Hitbox(x + subWidth, y + subHeight, subWidth, subHeight));
	}
	
	/**
	 * Determines which node the object belongs to.
	 * @return the index that the object should belong to, or -1 if it should stay in the parent tree.
	 */
	private <H extends IHitboxed> int getIndex(H obj) {
		int index = -1;
		double verticalMidpoint = bounds.getX() + (bounds.getWidth() / 2D);
		double horizontalMidpoint = bounds.getY() + (bounds.getHeight() / 2D);

		// Object can completely fit within the top quadrants
		boolean topQuadrant = (obj.getHitbox().getY() < horizontalMidpoint
				&& obj.getHitbox().getY() + obj.getHitbox().getHeight() < horizontalMidpoint);
		// Object can completely fit within the bottom quadrants
		boolean bottomQuadrant = (obj.getHitbox().getY() >= horizontalMidpoint);

		// Object can completely fit within the left quadrants
		if (obj.getHitbox().getX() < verticalMidpoint
				&& obj.getHitbox().getX() + obj.getHitbox().getWidth() < verticalMidpoint) {
			if (topQuadrant) {
				index = 1;
			} else if (bottomQuadrant) {
				index = 2;
			}
		}
		// Object can completely fit within the right quadrants
		else if (obj.getHitbox().getX() >= verticalMidpoint) {
			if (topQuadrant) {
				index = 0;
			} else if (bottomQuadrant) {
				index = 3;
			}
		}

		return index;
	}
	
	/**
	 * Insert the object into the QuadTree. The
	 * tree is automatically split if it overflows.
	 */
	public <H extends IHitboxed> void insert(H obj) {
		if (subnodes[0] != null) {
			int index = getIndex(obj);

			if (index != -1) {
				subnodes[index].insert(obj);

				return;
			}
		}

		objects.add(obj);

		if (objects.size() > MAX_OBJECTS && depth < MAX_DEPTH) {
			if (subnodes[0] == null) {
				split();
			}
			int i = 0;
			while (i < objects.size()) {
				int index = getIndex(objects.get(i));
				if (index != -1) {
					subnodes[index].insert(objects.remove(i));
				} else {
					i++;
				}
			}
		}
	}
	
	public <H extends IHitboxed> LinkedList<IHitboxed> getPossibleCollisionsOf(H obj) {
		LinkedList<IHitboxed> possibleCollisions = new LinkedList<>();
		
		int index = getIndex(obj);
		if (index != -1 && subnodes[0] != null) {
			possibleCollisions.addAll(subnodes[index].getPossibleCollisionsOf(obj));
		}

		possibleCollisions.addAll(objects);

		return possibleCollisions;
	}

}
