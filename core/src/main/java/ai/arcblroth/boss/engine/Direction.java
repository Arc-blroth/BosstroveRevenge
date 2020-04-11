package ai.arcblroth.boss.engine;

import java.util.ArrayList;

public enum Direction {
	NORTH(0),
	EAST(1),
	SOUTH(2),
	WEST(3);

	private transient final byte mask;

	Direction(int ordinal) {
		mask = (byte) (1 << ordinal);
	}

	public byte getMask() {
		return mask;
	}

	public static Direction[] maskToDirections(byte mask) {
		ArrayList<Direction> directions = new ArrayList<>(4);
		if((mask & NORTH.getMask()) != 0) {
			directions.add(NORTH);
		}
		if((mask & EAST.getMask()) != 0) {
			directions.add(EAST);
		}
		if((mask & SOUTH.getMask()) != 0) {
			directions.add(SOUTH);
		}
		if((mask & WEST.getMask()) != 0) {
			directions.add(WEST);
		}
		return (Direction[]) directions.toArray();
	}

	public static byte directionsToMask(Direction... directions) {
		byte mask = 0;
		for(Direction dir : directions) {
			mask = (byte) (mask | dir.getMask());
		}
		return mask;
	}

}
