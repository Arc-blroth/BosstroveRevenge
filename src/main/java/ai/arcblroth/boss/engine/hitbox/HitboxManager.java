package ai.arcblroth.boss.engine.hitbox;

import java.util.LinkedList;
import ai.arcblroth.boss.engine.IHitboxed;

public class HitboxManager {
	
	private LinkedList<IHitboxed> objects;
	private QuadTree quadtree;
	private volatile boolean isQuadtreeValid;
	
	public HitboxManager(int width, int height) {
		this.objects = new LinkedList<IHitboxed>();
		quadtree = new QuadTree(new Hitbox(0, 0, width, height));
		isQuadtreeValid = true;
	}
	
	public void clear() {
		objects.clear();
		isQuadtreeValid = false;
	}
	
	public <H extends IHitboxed> void add(H object) {
		objects.add(object);
		isQuadtreeValid = false;
	}
	
	public <H extends IHitboxed> void remove(H object) {
		objects.remove(object);
		isQuadtreeValid = false;
	}
	
	private synchronized void validateQuadtree() {
		if(!isQuadtreeValid) {
			quadtree.clear();
			objects.forEach(quadtree::insert);
			isQuadtreeValid = true;
		}
	}
	
	public LinkedList<IHitboxed> getAllCollisionsOf(IHitboxed obj) {
		validateQuadtree();
		LinkedList<IHitboxed> possibleCollisions = quadtree.getPossibleCollisionsOf(obj);
		possibleCollisions.removeIf(other -> 
			obj == other || !obj.getHitbox().intersects(other.getHitbox())
		);
		return possibleCollisions;
	}
	
}
