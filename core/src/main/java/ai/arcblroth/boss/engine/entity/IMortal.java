package ai.arcblroth.boss.engine.entity;

public interface IMortal {
	
	public double getHealth();
	
	public void setHealth(double health);
	
	public void damage(double baseDamage);
	
	public void heal(double baseHealth);
	
}
