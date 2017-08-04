package one.lindegaard.MobHunting.compatibility;

import me.eccentric_nz.tardisweepingangels.TARDISWeepingAngelSpawnEvent;
import me.eccentric_nz.tardisweepingangels.TARDISWeepingAngels;
import me.eccentric_nz.tardisweepingangels.utils.Monster;
import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import one.lindegaard.MobHunting.mobs.MobPlugin;
import one.lindegaard.MobHunting.rewards.RewardData;
import one.lindegaard.MobHunting.storage.IDataStore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class TARDISWeepingAngelsCompat implements Listener {

	public  static final String MH_TARDISWEEPINGANGELS = "MH:TARDISWeepingAngels";
	private  TARDISWeepingAngels mPlugin;
	private  boolean supported = false;
	private  HashMap<String, RewardData> mMobRewardData = new HashMap<String, RewardData>();
	private  File file = new File(MobHunting.getInstance().getDataFolder(), "TARDISWeepingAngels-rewards.yml");
	private  YamlConfiguration config = new YamlConfiguration();
	private Messages messages;
	private IDataStore iDataStore;
    private ConfigManager configManager;
    private ExtendedMobManager extendedMobManager;

	// http://dev.bukkit.org/bukkit-plugins/tardisweepingangels/

	public TARDISWeepingAngelsCompat(Messages messages, IDataStore iDataStore, ConfigManager configManager, ExtendedMobManager extendedMobManager) {
        this.messages = messages;
        this.iDataStore = iDataStore;
        this.configManager = configManager;
        this.extendedMobManager = extendedMobManager;
        if (isDisabledInConfig()) {
			Bukkit.getLogger().info("[MobHunting] Compatibility with TARDISWeepingAngels is disabled in config.yml");
		} else {
			mPlugin = (TARDISWeepingAngels) Bukkit.getPluginManager().getPlugin("TARDISWeepingAngels");

			Bukkit.getLogger().info("[MobHunting] Enabling compatibility with TARDISWeepingAngelsAPI ("
					+ mPlugin.getDescription().getVersion() + ")");

			supported = true;

			Bukkit.getPluginManager().registerEvents(this, MobHunting.getInstance());

			loadTARDISWeepingAngelsMobsData();
			saveTARDISWeepingAngelsMobsData();

		}
	}

	// **************************************************************************
	// OTHER
	// **************************************************************************

	public  TARDISWeepingAngels getTARDISWeepingAngels() {
		return mPlugin;
	}

	public  boolean isSupported() {
		return supported;
	}

	public  boolean isDisabledInConfig() {
		return configManager.disableIntegrationTARDISWeepingAngels;
	}

	public  boolean isEnabledInConfig() {
		return !configManager.disableIntegrationTARDISWeepingAngels;
	}

	/**
	 * Returns whether an entity is a TARDISWeepingAngels entity.
	 *
	 * @param entity
	 *            the entity to check
	 * @return true if the entity is a TARDISWeepingAngels entity
	 */
	public  boolean isWeepingAngelMonster(Entity entity) {
		return isSupported() && entity.hasMetadata(TARDISWeepingAngelsCompat.MH_TARDISWEEPINGANGELS);
	}

	/**
	 * Returns the Monster type for a TARDISWeepingAngels entity.
	 *
	 * @param entity
	 *            the entity to get the Monster type for
	 * @return the Monster type or null if it is not TARDISWeepingAngels entity
	 */
	public  Monster getWeepingAngelMonsterType(Entity entity) {
		return mPlugin.getWeepingAngelsAPI().getWeepingAngelMonsterType(entity);
	}

	public  HashMap<String, RewardData> getMobRewardData() {
		return mMobRewardData;
	}

	// **************************************************************************
	// LOAD & SAVE
	// **************************************************************************
	public  void loadTARDISWeepingAngelsMobsData() {
		try {
			if (!file.exists()) {
				for (Monster monster : Monster.getValues()) {
					mMobRewardData.put(monster.name(),
							new RewardData(MobPlugin.TARDISWeepingAngels, monster.name(), monster.getName(), "40:60",
									"minecraft:give {player} iron_sword 1", "You got an Iron sword.", 1, 1, 0.02));
					saveTARDISWeepingAngelsMobsData(mMobRewardData.get(monster.name()).getMobType());
                    iDataStore.insertTARDISWeepingAngelsMobs(monster.name);
				}
				messages.injectMissingMobNamesToLangFiles();
				return;
			}

			config.load(file);
			for (String key : config.getKeys(false)) {
				ConfigurationSection section = config.getConfigurationSection(key);
				RewardData mob = new RewardData();
				mob.read(section);
				mob.setMobType(key);
				mMobRewardData.put(key, mob);
                iDataStore.insertTARDISWeepingAngelsMobs(key);
			}
			messages.injectMissingMobNamesToLangFiles();
			messages.debug("Loaded %s TARDISWeepingAngels-Mobs", mMobRewardData.size());
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}

    }

	public  void loadTARDISWeepingAngelsMobsData(String key) {
		try {
			if (!file.exists()) {
				return;
			}

			config.load(file);
			ConfigurationSection section = config.getConfigurationSection(key);
			RewardData mob = new RewardData();
			mob.read(section);
			mob.setMobType(key);
			mMobRewardData.put(key, mob);
            iDataStore.insertTARDISWeepingAngelsMobs(key);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public  void saveTARDISWeepingAngelsMobsData() {
		try {
			config.options().header("This a extra MobHunting config data for the TARDISWeepingAngels on your server.");

			if (mMobRewardData.size() > 0) {

				int n = 0;
				for (String str : mMobRewardData.keySet()) {
					ConfigurationSection section = config.createSection(str);
					mMobRewardData.get(str).save(section);
					n++;
				}

				if (n != 0) {
					messages.debug("Saving Mobhunting extra TARDISWeepingAngels data.");
					config.save(file);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public  void saveTARDISWeepingAngelsMobsData(String key) {
		try {
			if (mMobRewardData.containsKey(key)) {
				ConfigurationSection section = config.createSection(key);
				mMobRewardData.get(key).save(section);
				messages.debug("Saving extra TARDISWeepingAngels data for mob=%s (%s)", key,
						mMobRewardData.get(key).getMobName());
				config.save(file);
			} else {
				messages.debug("ERROR! TARDISWeepingAngels ID (%s) is not found in mMobRewardData", key);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// **************************************************************************
	// EVENTS
	// **************************************************************************

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	private void onTARDISWeepingAngelSpawnEvent(TARDISWeepingAngelSpawnEvent event) {

		Entity entity = event.getEntity();
		Monster monster = getWeepingAngelMonsterType(entity);

		if (mMobRewardData != null && !mMobRewardData.containsKey(monster.name())) {
			messages.debug("New TARDIS mob found=%s (%s)", monster.name(), monster.getName());
			mMobRewardData.put(monster.name(),
					new RewardData(MobPlugin.TARDISWeepingAngels, monster.name(), monster.getName(), "40:60",
							"minecraft:give {player} iron_sword 1", "You got an Iron sword.", 1, 1, 0.02));
			saveTARDISWeepingAngelsMobsData(monster.name());
            iDataStore.insertTARDISWeepingAngelsMobs(monster.name);
			// Update mob loaded into memory
			extendedMobManager.updateExtendedMobs();
			messages.injectMissingMobNamesToLangFiles();
		}

		event.getEntity().setMetadata(MH_TARDISWEEPINGANGELS,
				new FixedMetadataValue(mPlugin, mMobRewardData.get(monster.name())));
	}

	public  int getProgressAchievementLevel1(String mobtype) {
		return mMobRewardData.get(mobtype).getAchivementLevel1();
	}

}
