package one.lindegaard.MobHunting.compatibility;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import one.lindegaard.MobHunting.mobs.MobPlugin;
import one.lindegaard.MobHunting.rewards.RewardData;
import one.lindegaard.MobHunting.storage.IDataStore;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public class MythicMobsV400Compat implements Listener {

	private  Plugin mPlugin;

	private ConfigManager configManager;
	private IDataStore iDataStore;
	private ExtendedMobManager extendedMobManager;
	private Messages messages;
	private MythicMobsCompat mythicMobsCompat;


	public MythicMobsV400Compat(ConfigManager configManager, IDataStore iDataStore, ExtendedMobManager extendedMobManager, Messages messages, MythicMobsCompat mythicMobsCompat) {
        this.configManager = configManager;
        this.iDataStore = iDataStore;
        this.extendedMobManager = extendedMobManager;
        this.messages = messages;
		this.mythicMobsCompat = mythicMobsCompat;
		mPlugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
	}

	// **************************************************************************
	// OTHER FUNCTIONS
	// **************************************************************************
	private  MythicMobs getMythicMobsV400() {
		return (MythicMobs) mPlugin;
	}

	public  boolean isMythicMobV400(String killed) {
		return mythicMobsCompat.isSupported() && getMythicMobV400(killed) != null;
	}

	public  MythicMob getMythicMobV400(String killed) {
		if (mythicMobsCompat.isSupported())
			return getMythicMobsV400().getAPIHelper().getMythicMob(killed);
		return null;
	}

	public  boolean isDisabledInConfig() {
		return configManager.disableIntegrationMythicmobs;
	}

	public  boolean isEnabledInConfig() {
		return !isDisabledInConfig();
	}

	// **************************************************************************
	// EVENTS
	// **************************************************************************

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	private void onMythicMobV400SpawnEvent(MythicMobSpawnEvent event) {
		String mobtype = event.getMobType().getInternalName();
		messages.debug("MythicMobSpawnEvent: MythicMobType=%s", mobtype);

		if (!mythicMobsCompat.getMobRewardData().containsKey(mobtype)) {
			messages.debug("New MythicMobType found=%s (%s)", mobtype, event.getMobType().getDisplayName());
			mythicMobsCompat.getMobRewardData().put(mobtype,
					new RewardData(MobPlugin.MythicMobs, mobtype, event.getMobType().getDisplayName(), "10",
							"minecraft:give {player} iron_sword 1", "You got an Iron sword.", 1, 1, 0.02));
			mythicMobsCompat.saveMythicMobsData(mobtype);
			iDataStore.insertMissingMythicMobs(mobtype);
			// Update mob loaded into memory
			extendedMobManager.updateExtendedMobs();
			messages.injectMissingMobNamesToLangFiles();
		}

		event.getEntity().setMetadata(MythicMobsCompat.MH_MYTHICMOBS, new FixedMetadataValue(mPlugin,
				mythicMobsCompat.getMobRewardData().get(event.getMobType().getInternalName())));
	}

	@SuppressWarnings("unused")
	private void onMythicMobV400DeathEvent(MythicMobDeathEvent event) {

	}

}
