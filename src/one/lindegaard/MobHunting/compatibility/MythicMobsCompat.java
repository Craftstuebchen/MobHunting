package one.lindegaard.MobHunting.compatibility;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.StatType;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import one.lindegaard.MobHunting.rewards.RewardData;
import one.lindegaard.MobHunting.storage.IDataStore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class MythicMobsCompat {

	private final ConfigManager configManager;
	private final IDataStore iDataStore;
	private final ExtendedMobManager extendedMobManager;
	private final Messages messages;
	public  MythicMobVersion mmVersion = MythicMobVersion.NOT_DETECTED;

	public static final String MH_MYTHICMOBS = "MH:MYTHICMOBS";
    private  boolean supported = false;
	private  Plugin mPlugin;
	private  HashMap<String, RewardData> mMobRewardData = new HashMap<String, RewardData>();
	private  File file = new File(MobHunting.getInstance().getDataFolder(), "mythicmobs-rewards.yml");
	private  YamlConfiguration config = new YamlConfiguration();

	public enum MythicMobVersion {
		NOT_DETECTED, MYTHICMOBS_V251, MYTHICMOBS_V400
	};
	private MythicMobsV400Compat mythicMobsV400Compat;

	public MythicMobsCompat(ConfigManager configManager, IDataStore iDataStore, ExtendedMobManager extendedMobManager, Messages messages) {
        this.configManager = configManager;
		this.iDataStore = iDataStore;
		this.extendedMobManager = extendedMobManager;
		this.messages = messages;
		if (isDisabledInConfig()) {
			Bukkit.getLogger().info("[MobHunting] Compatibility with MythicMobs is disabled in config.yml");
		} else {
			mPlugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
			if (mPlugin.getDescription().getVersion().compareTo("4.0.0") >= 0) {

				Bukkit.getLogger().info("[MobHunting] Enabling compatibility with MythicMobs ("
						+ mPlugin.getDescription().getVersion() + ")");
				mmVersion = MythicMobVersion.MYTHICMOBS_V400;
				supported = true;
				this.mythicMobsV400Compat = new MythicMobsV400Compat(this.configManager, this.iDataStore, this.extendedMobManager, this.messages, this);
				Bukkit.getPluginManager().registerEvents(mythicMobsV400Compat, MobHunting.getInstance());

			} else if (mPlugin.getDescription().getVersion().compareTo("2.5.1") >= 0) {
				Bukkit.getLogger().info("[MobHunting] Enabling compatibility with MythicMobs ("
						+ mPlugin.getDescription().getVersion() + ")");
				mmVersion = MythicMobVersion.MYTHICMOBS_V251;
				supported = true;
				Bukkit.getPluginManager().registerEvents(new MythicMobsV251Compat(), MobHunting.getInstance());

			} else {
				ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
				console.sendMessage(ChatColor.RED
						+ "[MobHunting] MythicMobs is outdated. Please update to V2.5.1 or newer. Integration will be disabled");
				return;
			}
			loadMythicMobsData();
			saveMythicMobsData();
		}
	}

	public  boolean isSupported() {
		return supported;
	}

	public  void setSupported(boolean status) {
		supported = status;
	}

	public  MythicMobVersion getMythicMobVersion() {
		return mmVersion;
	}

	public  boolean isDisabledInConfig() {
		return configManager.disableIntegrationMythicmobs;
	}

	public  boolean isEnabledInConfig() {
		return !isDisabledInConfig();
	}

	public  HashMap<String, RewardData> getMobRewardData() {
		return mMobRewardData;
	}

	public  boolean isMythicMob(String mob) {
		switch (mmVersion) {
		case MYTHICMOBS_V251:
			return MythicMobsV251Compat.isMythicMobV251(mob);
		case MYTHICMOBS_V400:
			return mythicMobsV400Compat.isMythicMobV400(mob);
		case NOT_DETECTED:
			break;
		default:
			break;
		}
		return false;
	}

	public  boolean isMythicMob(Entity killed) {
		if (isSupported())
			return killed.hasMetadata(MH_MYTHICMOBS);
		return false;
	}

	public  String getMythicMobType(Entity killed) {
		List<MetadataValue> data = killed.getMetadata(MythicMobsCompat.MH_MYTHICMOBS);
		for (MetadataValue mdv : data) {
			if (mdv.value() instanceof RewardData)
				return ((RewardData) mdv.value()).getMobType();
		}
		return null;
	}

	public  int getProgressAchievementLevel1(String mobtype) {
		return mMobRewardData.get(mobtype).getAchivementLevel1();
	}

	// **************************************************************************
	// LOAD & SAVE
	// **************************************************************************
	public  void loadMythicMobsData() {
		try {
			if (!file.exists())
				return;
			messages.debug("Loading extra MobRewards for MythicMobs mobs.");

			config.load(file);
			int n = 0;
			for (String key : config.getKeys(false)) {
				ConfigurationSection section = config.getConfigurationSection(key);
				if (isMythicMob(key)) {
					RewardData mob = new RewardData();
					mob.read(section);
					mob.setMobType(key);
					mMobRewardData.put(key, mob);
					iDataStore.insertMissingMythicMobs(key);
					n++;
				} else {
					messages.debug("The mob=%s can't be found in MythicMobs configuration files", key);
				}
			}
			messages.injectMissingMobNamesToLangFiles();
			messages.debug("Loaded %s MythicMobs", n);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}

	}

	public  void loadMythicMobsData(String key) {
		try {
			if (!file.exists())
				return;

			config.load(file);
			ConfigurationSection section = config.getConfigurationSection(key);
			if (isMythicMob(key)) {
				RewardData mob = new RewardData();
				mob.read(section);
				mob.setMobType(key);
				mMobRewardData.put(key, mob);
				int n = StatType.values().length;
				StatType.values()[n + 1] = new StatType(mob.getMobType() + "_kill", mob.getMobName());
				StatType.values()[n + 2] = new StatType(mob.getMobType() + "_assist", mob.getMobName());
				iDataStore.insertMissingMythicMobs(key);
			} else {
				messages.debug("The mob=%s can't be found in MythicMobs configuration files", key);
			}
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public  void saveMythicMobsData() {
		try {
			config.options().header("This a extra MobHunting config data for the MythicMobs on your server.");

			if (mMobRewardData.size() > 0) {

				int n = 0;
				for (String str : mMobRewardData.keySet()) {
					ConfigurationSection section = config.createSection(str);
					mMobRewardData.get(str).save(section);
					n++;
				}

				if (n != 0) {
					messages.debug("Saving Mobhunting extra MythicMobs data.");
					config.save(file);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public  void saveMythicMobsData(String key) {
		try {
			if (mMobRewardData.containsKey(key)) {
				ConfigurationSection section = config.createSection(key);
				mMobRewardData.get(key).save(section);
				messages.debug("Saving Mobhunting extra MythicMobs data.");
				config.save(file);
			} else {
				messages.debug("ERROR! MythicMobs ID (%s) is not found in mMobRewardData", key);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
