package one.lindegaard.MobHunting.compatibility;

import one.lindegaard.MobHunting.ConfigManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class DisguisesHelper {

	// ***************************************************************************
	// Integration to LibsDisguises, DisguiseCraft, IDisguise
	// ***************************************************************************

	private ConfigManager configManager;
	private IDisguiseCompat iDisguiseCompat;

    public DisguisesHelper(ConfigManager configManager, IDisguiseCompat iDisguiseCompat) {
        this.configManager = configManager;
        this.iDisguiseCompat = iDisguiseCompat;
    }


    /**
	 * isDisguised - checks if the player is disguised.
	 * 
	 * @param entity
	 * @return true when the player is disguised and false when the is not.
	 */
	public  boolean isDisguised(Entity entity) {
		if (CompatibilityManager.isPluginLoaded(LibsDisguisesCompat.class) && entity instanceof Player
				&& !configManager.disableIntegrationLibsDisguises)
			return LibsDisguisesCompat.isDisguised((Player) entity);
		else if (CompatibilityManager.isPluginLoaded(DisguiseCraftCompat.class) && entity instanceof Player
				&& !configManager.disableIntegrationDisguiseCraft)
			return DisguiseCraftCompat.isDisguised((Player) entity);
		else if (CompatibilityManager.isPluginLoaded(IDisguiseCompat.class) && entity instanceof Player
				&& !configManager.disableIntegrationIDisguise)
			return iDisguiseCompat.isDisguised(entity);
		else {
			return false;
		}
	}

	/**
	 * isDisguisedAsAsresiveMob - checks if the player is disguised as a mob who
	 * attacks players.
	 * 
	 * @param entity
	 * @return true when the player is disguised as a mob who attacks players
	 *         and false when not.
	 */
	public  boolean isDisguisedAsAgresiveMob(Entity entity) {
		if (CompatibilityManager.isPluginLoaded(LibsDisguisesCompat.class) && entity instanceof Player
				&& !configManager.disableIntegrationLibsDisguises)
			return LibsDisguisesCompat.isAggresiveDisguise(entity);
		else if (CompatibilityManager.isPluginLoaded(DisguiseCraftCompat.class) && entity instanceof Player
				&& !configManager.disableIntegrationDisguiseCraft)
			return DisguiseCraftCompat.isAggresiveDisguise(entity);
		else if (CompatibilityManager.isPluginLoaded(IDisguiseCompat.class) && entity instanceof Player
				&& !configManager.disableIntegrationIDisguise)
			return iDisguiseCompat.isAggresiveDisguise(entity);
		else {
			return false;
		}
	}

	/**
	 * isDisguisedAsPlayer - checks if the player is disguised as another
	 * player.
	 * 
	 * @param entity
	 * @return true when the player is disguised as another player, and false
	 *         when not.
	 */
	public  boolean isDisguisedAsPlayer(Entity entity) {
		if (CompatibilityManager.isPluginLoaded(LibsDisguisesCompat.class) && entity instanceof Player
				&& !configManager.disableIntegrationLibsDisguises)
			return LibsDisguisesCompat.isPlayerDisguise((Player) entity);
		else if (CompatibilityManager.isPluginLoaded(DisguiseCraftCompat.class) && entity instanceof Player
				&& !configManager.disableIntegrationDisguiseCraft)
			return DisguiseCraftCompat.isPlayerDisguise((Player) entity);
		else if (CompatibilityManager.isPluginLoaded(IDisguiseCompat.class) && entity instanceof Player
				&& !configManager.disableIntegrationIDisguise)
			return iDisguiseCompat.isPlayerDisguise((Player) entity);
		else {
			return false;
		}
	}

	public  void undisguiseEntity(Entity entity) {
		if (CompatibilityManager.isPluginLoaded(LibsDisguisesCompat.class)
				&& !configManager.disableIntegrationLibsDisguises)
			LibsDisguisesCompat.undisguiseEntity(entity);
		else if (CompatibilityManager.isPluginLoaded(DisguiseCraftCompat.class)
				&& !configManager.disableIntegrationDisguiseCraft)
			DisguiseCraftCompat.undisguisePlayer(entity);
		else if (CompatibilityManager.isPluginLoaded(IDisguiseCompat.class)
				&& !configManager.disableIntegrationIDisguise)
			iDisguiseCompat.undisguisePlayer(entity);
		else {

		}
	}
}
