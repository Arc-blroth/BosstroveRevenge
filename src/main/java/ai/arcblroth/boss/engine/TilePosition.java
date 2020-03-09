package ai.arcblroth.boss.engine;

import ai.arcblroth.boss.util.Pair;

public class TilePosition extends Pair<Integer, Integer> {

	public TilePosition(int x, int y) {
		super(x, y);
	}
	
	public int getX() {
		return getFirst();
	}
	
	public int getY() {
		return getSecond();
	}

}
