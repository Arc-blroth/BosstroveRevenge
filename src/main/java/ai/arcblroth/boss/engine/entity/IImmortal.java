package ai.arcblroth.boss.engine.entity;

public interface IImmortal extends IMortal {
	
	public default double getHealth() {return Double.MAX_VALUE;}
	
	public default void setHealth(double health) {}
	
	public default void damage(double baseDamage) {}
	
	public default void heal(double baseHealth) {}
	
}
