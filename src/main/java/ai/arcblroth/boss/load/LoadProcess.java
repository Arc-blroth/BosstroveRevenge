package ai.arcblroth.boss.load;

import ai.arcblroth.boss.engine.tile.EmptyFloorTile;
import ai.arcblroth.boss.engine.tile.EmptyWallTile;
import ai.arcblroth.boss.register.FloorTileRegistry;
import ai.arcblroth.boss.register.WallTileRegistry;
import ai.arcblroth.boss.util.Pair;

public class LoadProcess extends Thread {
	
	public enum Phase {
		EARLY_LOADING(0, "Loading"),
		REGISTERING_TILES(1, "Registering tiles"),
		DONE(2, "Done");
		
		private int index;
		private String name;

		Phase(int index, String name) {
			this.index = index;
			this.name = name;
		}
		
		public int getIndex() {
			return index;
		}
		
		public String getPhaseName() {
			return name;
		}
	}
	
	private Phase phase = Phase.values()[0];
	private boolean doneYet = false;
	
	public void run() {
		phase = Phase.REGISTERING_TILES;
		FloorTileRegistry.register("empty", new EmptyFloorTile());
		WallTileRegistry.register("empty", new EmptyWallTile());
		phase = Phase.DONE;
		doneYet = true;
	}
	
	public Pair<Double, String> getProgressRecord() {
		return new Pair<Double, String>(((double)phase.getIndex() + 1D) / (double)Phase.values().length, phase.getPhaseName());
	}
	
	public boolean isDone() {
		return doneYet;
	}
	
}
