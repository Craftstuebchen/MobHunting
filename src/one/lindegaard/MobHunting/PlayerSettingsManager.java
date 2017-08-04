package one.lindegaard.MobHunting;

import one.lindegaard.MobHunting.storage.DataStoreManager;
import one.lindegaard.MobHunting.storage.IDataCallback;
import one.lindegaard.MobHunting.storage.PlayerSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.UUID;

public class PlayerSettingsManager implements Listener {

	private HashMap<UUID, PlayerSettings> mPlayerSettings = new HashMap<>();

	private MobHunting plugin;
	private DataStoreManager dataStoreManager;
	private ConfigManager configManager;
	private Messages messages;

	/**
	 * Constructor for the PlayerSettingsmanager
	 */
	PlayerSettingsManager(MobHunting plugin) {
		this.plugin=plugin;
		this.dataStoreManager=this.plugin.getDataStoreManager();
		this.configManager=plugin.getConfigManager();
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	/**
	 * Get playerSettings from memory
	 * 
	 * @param offlinePlayer
	 * @return PlayerSettings
	 */
	public PlayerSettings getPlayerSettings(OfflinePlayer offlinePlayer) {
		if (mPlayerSettings.containsKey(offlinePlayer.getUniqueId()))
			return mPlayerSettings.get(offlinePlayer.getUniqueId());
		else
			return new PlayerSettings(configManager,offlinePlayer);
	}

	/**
	 * Store playerSettings in memory
	 * 
	 * @param playerSettings
	 */
	public void setPlayerSettings(OfflinePlayer player, PlayerSettings playerSettings) {
		mPlayerSettings.put(player.getUniqueId(), playerSettings);
	}

	/**
	 * Remove PlayerSettings from Memory
	 * 
	 * @param player
	 */
	public void removePlayerSettings(OfflinePlayer player) {
		messages.debug("Removing %s from player settings cache", player.getName());
		mPlayerSettings.remove(player.getUniqueId());
	}

	/**
	 * Read PlayerSettings From database into Memory when player joins
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	private void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		if (containsKey(player))
			messages.debug("Using cached player settings");
		else
			load(player);
	}

	/**
	 * Write PlayerSettings to Database when Player Quit and remove
	 * PlayerSettings from memory
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	private void onPlayerQuit(PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		save(player);
	}

	/**
	 * Load PlayerSettings asynchronously from Database
	 * 
	 * @param player
	 */
	public void load(final OfflinePlayer player) {
		dataStoreManager.requestPlayerSettings(player, new IDataCallback<PlayerSettings>() {

			@Override
			public void onCompleted(PlayerSettings ps) {
				if (ps.isMuted())
                    messages.debug("%s isMuted()", player.getName());
				if (ps.isLearningMode())
                    messages.debug("%s is in LearningMode()", player.getName());
				mPlayerSettings.put(player.getUniqueId(), ps);
			}

			@Override
			public void onError(Throwable error) {
				Bukkit.getConsoleSender().sendMessage(
						ChatColor.RED + "[MobHunting][ERROR] Could not load playerSettings for " + player.getName());
				mPlayerSettings.put(player.getUniqueId(),new PlayerSettings(configManager,player));
			}
		});
	}

	/**
	 * Write PlayerSettings to Database
	 * 
	 * @param player
	 */
	public void save(final OfflinePlayer player) {
		dataStoreManager.updatePlayerSettings(player, getPlayerSettings(player).isLearningMode(),
				getPlayerSettings(player).isMuted());
	}

	/**
	 * Test if PlayerSettings contains data for Player
	 * 
	 * @param player
	 * @return true if player exists in PlayerSettings in Memory
	 */
	public boolean containsKey(final OfflinePlayer player) {
		return mPlayerSettings.containsKey(player.getUniqueId());
	}

}
