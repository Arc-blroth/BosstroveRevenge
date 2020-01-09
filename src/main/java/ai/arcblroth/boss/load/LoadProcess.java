package ai.arcblroth.boss.load;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.engine.tile.EmptyFloorTile;
import ai.arcblroth.boss.engine.tile.EmptyWallTile;
import ai.arcblroth.boss.register.FloorTileRegistry;
import ai.arcblroth.boss.register.WallTileRegistry;
import ai.arcblroth.boss.resource.InternalResource;
import ai.arcblroth.boss.resource.Resource;
import ai.arcblroth.boss.resource.load.ITileLoader;
import ai.arcblroth.boss.resource.load.ResourceLoader;
import ai.arcblroth.boss.util.Pair;
import ai.arcblroth.boss.util.ThreadUtils;

public class LoadProcess extends Thread {
	
	public enum Phase {
		EARLY_LOADING(0, "Loading"),
		REGISTERING_TILES(1, "Registering tiles"),
		DONE(2, "Done");
		
		private int index;
		private String name;
		private Logger logger;

		Phase(int index, String name) {
			this.index = index;
			this.name = name;
			this.logger = Logger.getLogger("PHASE_" + this.name());
		}
		
		public int getIndex() {
			return index;
		}
		
		public String getPhaseName() {
			return name;
		}
		
		public Logger getLogger() {
			return logger;
		}
	}
	
	private Phase phase = Phase.values()[0];
	private boolean doneYet = false;
	
	public void run() {
		phase = Phase.EARLY_LOADING;
		
		// Scan jar for resources.
		LinkedList<Resource> data = new LinkedList<Resource>();
		phase.getLogger().log(Level.INFO, "Scanning jar resources...");
		try {
			Stream<Resource> dataStream = ResourceLoader.getAllResourcesFromInternalFolder(new InternalResource("data/"), true);
			dataStream.forEach((res) -> data.add(res));
			dataStream.close();
		} catch (Exception e) {
			phase.getLogger().log(Level.SEVERE, "Could not scan jar file for resources!", e);
			BosstrovesRevenge.get().shutdown(-1);
		}
		
		phase = Phase.REGISTERING_TILES;
		
		FloorTileRegistry.get().register("empty", new EmptyFloorTile());
		WallTileRegistry.get().register("empty", new EmptyWallTile());
		
		ITileLoader tileLoader = new ITileLoader(BosstrovesRevenge.get().getTextureCache());
		
		data.stream()
			.filter((res) -> res.getPath().endsWith(ITileLoader.BTILE_EXTENSION))
			.forEach((res) -> tileLoader.register(res));
		
		FloorTileRegistry.get().forEach((key, tile) -> phase.getLogger().log(Level.INFO, "FloorTile: " + key));
		WallTileRegistry.get().forEach((key, tile) -> phase.getLogger().log(Level.INFO, "WallTile: " + key));
		
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
