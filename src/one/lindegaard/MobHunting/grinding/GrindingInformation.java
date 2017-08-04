package one.lindegaard.MobHunting.grinding;

import one.lindegaard.MobHunting.ConfigManager;
import org.bukkit.entity.Entity;

public class GrindingInformation {

	private int entityId;
	private Entity killed;
	private long timeOfDeath;
	private double cDampnerRange;

	GrindingInformation(ConfigManager configManager,Entity killed) {
		entityId = killed.getEntityId();
		this.killed = killed;
		timeOfDeath=System.currentTimeMillis();
		cDampnerRange = configManager.grindingDetectionRange;
	}

	/**
	 * @return the entityId
	 */
	public int getEntityId() {
		return entityId;
	}

	/**
	 * @return the killed
	 */
	public Entity getKilled() {
		return killed;
	}

	/**
	 * @return the timeOfDeath
	 */
	public long getTimeOfDeath() {
		return timeOfDeath;
	}

	/**
	 * @param timeOfDeath the timeOfDeath to set
	 */
	public void setTimeOfDeath(long timeOfDeath) {
		this.timeOfDeath = timeOfDeath;
	}

	/**
	 * @return the cDampnerRange
	 */
	public double getcDampnerRange() {
		return cDampnerRange;
	}

	/**
	 * @param cDampnerRange the cDampnerRange to set
	 */
	public void setcDampnerRange(double cDampnerRange) {
		this.cDampnerRange = cDampnerRange;
	}

}
