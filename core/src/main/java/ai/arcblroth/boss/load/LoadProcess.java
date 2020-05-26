package ai.arcblroth.boss.load;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.engine.tile.EmptyFloorTile;
import ai.arcblroth.boss.engine.tile.EmptyWallTile;
import ai.arcblroth.boss.register.AreaRegistry;
import ai.arcblroth.boss.register.EntityRegistry;
import ai.arcblroth.boss.register.FloorTileRegistry;
import ai.arcblroth.boss.register.WallTileRegistry;
import ai.arcblroth.boss.resource.InternalResource;
import ai.arcblroth.boss.resource.Resource;
import ai.arcblroth.boss.resource.load.*;
import ai.arcblroth.boss.util.Pair;
import ai.arcblroth.boss.util.StaticDefaults;
import com.google.gson.Gson;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class LoadProcess extends Thread {
	
	public enum Phase {
		EARLY_LOADING(0, "Loading"),
		REGISTERING_TILES(1, "Registering tiles"),
		REGISTERING_ENTITIES(2, "Registering entities"),
		LOADING_LEVELS(3, "Loading levels"),
		DONE(4, "Done");
		
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
			dataStream.forEach(data::add);
			dataStream.close();
		} catch (Exception e) {
			phase.getLogger().log(Level.SEVERE, "Could not scan jar file for resources!", e);
			BosstrovesRevenge.instance().shutdown(-1);
		}
		
		phase = Phase.REGISTERING_TILES;
		
		FloorTileRegistry.instance().register("empty", null, StaticDefaults.DEFAULT_TILE_HITBOX, EmptyFloorTile::new);
		WallTileRegistry.instance().register("empty", null, StaticDefaults.DEFAULT_TILE_HITBOX, EmptyWallTile::new);
		
		Gson gson = new Gson();
		
		ITileLoader tileLoader = new ITileLoader(BosstrovesRevenge.instance().getTextureCache());
		
		data.stream()
			.filter((res) -> res.getPath().endsWith(ITileLoader.BTILE_EXTENSION))
			.forEach((res) -> tileLoader.register(gson, res));
		
		FloorTileRegistry.instance().forEach((key, tile) -> phase.getLogger().log(Level.INFO, "FloorTile: " + key));
		WallTileRegistry.instance().forEach((key, tile) -> phase.getLogger().log(Level.INFO, "WallTile: " + key));

		phase = Phase.REGISTERING_ENTITIES;
		
		IEntityLoader entLoader = new IEntityLoader();
		data.stream()
			.filter((res) -> res.getPath().endsWith(IEntityLoader.BENT_EXTENSION))
			.forEach((res) -> entLoader.register(gson, res));
		
		EntityRegistry.instance().forEach((key, entClass, entBuilder) -> phase.getLogger().log(Level.INFO, "Entity: " + key));

		AreaLoader areaLoader = new AreaLoader();
		data.stream()
				.filter((res) -> res.getPath().endsWith(AreaLoader.BAREA_EXTENSION))
				.forEach((res) -> areaLoader.register(gson, res));

		AreaRegistry.instance().forEach((key, areaBuildFunction) -> phase.getLogger().log(Level.INFO, "Area: " + key));
		
		phase = Phase.LOADING_LEVELS;
		
		ILevelLoader levelLoader = new ILevelLoader(BosstrovesRevenge.instance().getTextureCache());
		
		data.stream()
			.filter((res) -> res.getPath().endsWith(ILevelLoader.BLVL_EXTENSION))
			.forEach((res) -> levelLoader.register(gson, res));
		
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
