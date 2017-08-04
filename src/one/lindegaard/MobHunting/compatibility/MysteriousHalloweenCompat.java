package one.lindegaard.MobHunting.compatibility;

import me.F_o_F_1092.MysteriousHalloween.MysteriousHalloweenAPI;
import me.F_o_F_1092.MysteriousHalloween.MysteriousHalloweenAPI.MobType;
import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import one.lindegaard.MobHunting.mobs.MobPlugin;
import one.lindegaard.MobHunting.rewards.RewardData;
import one.lindegaard.MobHunting.storage.IDataStore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class MysteriousHalloweenCompat implements Listener {

    public static final String MH_MYSTERIOUSHALLOWEEN = "MH:MysteriousHalloween";
    private Plugin mPlugin;
    private boolean supported = false;
    private HashMap<String, RewardData> mMobRewardData = new HashMap<String, RewardData>();
    private File file = new File(MobHunting.getInstance().getDataFolder(), "MysteriousHalloween-rewards.yml");
    private YamlConfiguration config = new YamlConfiguration();
    private ConfigManager configManager;
    private Messages messages;
    private ExtendedMobManager extendedMobManager;
    private IDataStore iDataStore;


    // https://www.spigotmc.org/resources/mysterioushalloween.13059/

    public MysteriousHalloweenCompat(ConfigManager configManager, Messages messages, ExtendedMobManager extendedMobManager, IDataStore iDataStore) {
        this.configManager = configManager;
        this.messages = messages;
        this.extendedMobManager = extendedMobManager;
        this.iDataStore = iDataStore;
        if (isDisabledInConfig()) {
            Bukkit.getLogger().info("[MobHunting] Compatibility with MysteriousHalloween is disabled in config.yml");
        } else {
            mPlugin = Bukkit.getPluginManager().getPlugin("MysteriousHalloween");

            if (mPlugin.getDescription().getVersion().compareTo("1.3.2") >= 0) {

                Bukkit.getLogger().info("[MobHunting] Enabling compatibility with MysteriousHalloween ("
                        + mPlugin.getDescription().getVersion() + ")");

                supported = true;

                Bukkit.getPluginManager().registerEvents(this, MobHunting.getInstance());

                loadMysteriousHalloweenMobsData();
                saveMysteriousHalloweenMobsData();
            } else {
                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                console.sendMessage(ChatColor.RED + "[MobHunting] Your current version of MysteriousHalloween ("
                        + mPlugin.getDescription().getVersion()
                        + ") has no API implemented. Please update to V1.3.2 or newer.");
            }
        }
    }

    // **************************************************************************
    // OTHER
    // **************************************************************************

    public  boolean isSupported() {
        return supported;
    }

    public  boolean isDisabledInConfig() {
        return configManager.disableIntegrationMysteriousHalloween;
    }

    public  boolean isEnabledInConfig() {
        return !isDisabledInConfig();
    }

    /**
     * Returns whether an entity is a MysteriousHalloween entity.
     *
     * @param entity the entity to check
     * @return true if the entity is a MysteriousHalloween entity
     */
    public  boolean isMysteriousHalloween(Entity entity) {
        if (isSupported())
            return MysteriousHalloweenAPI.isEntity(entity);
        return false;
    }

    /**
     * Returns the Monster type for a MysteriousHalloween entity.
     *
     * @param entity the entity to get the mob type for
     * @return the mob type or null if it is not MysteriousHalloween entity
     */
    public  MobType getMysteriousHalloweenType(Entity entity) {
        if (isMysteriousHalloween(entity))
            return MysteriousHalloweenAPI.getMobType(entity);
        return null;
    }

    public  HashMap<String, RewardData> getMobRewardData() {
        return mMobRewardData;
    }

    // **************************************************************************
    // LOAD & SAVE
    // **************************************************************************
    public  void loadMysteriousHalloweenMobsData() {
        try {
            if (!file.exists()) {
                for (MobType monster : MysteriousHalloweenAPI.getMobTypes()) {
                    mMobRewardData.put(monster.name(),
                            new RewardData(MobPlugin.MysteriousHalloween, monster.name(),
                                    MysteriousHalloweenAPI.getMobTypeName(monster), "40:60",
                                    "minecraft:give {player} iron_sword 1", "You got an Iron sword.", 1, 1, 0.02));
                    saveMysteriousHalloweenMobsData(mMobRewardData.get(monster.name()).getMobType());
                }
                return;
            }

            config.load(file);
            for (String key : config.getKeys(false)) {
                ConfigurationSection section = config.getConfigurationSection(key);
                RewardData mob = new RewardData();
                mob.read(section);
                mob.setMobType(key);
                mMobRewardData.put(key, mob);
                iDataStore.insertMysteriousHalloweenMobs(key);
            }
            messages.injectMissingMobNamesToLangFiles();
            messages.debug("Loaded %s MysteriousHalloween-Mobs", mMobRewardData.size());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

    }

    public  void loadMysteriousHalloweenMobsData(String key) {
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
            iDataStore.insertMysteriousHalloweenMobs(key);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public  void saveMysteriousHalloweenMobsData() {
        try {
            config.options().header("This a extra MobHunting config data for the MysteriousHalloween on your server.");

            if (mMobRewardData.size() > 0) {

                int n = 0;
                for (String str : mMobRewardData.keySet()) {
                    ConfigurationSection section = config.createSection(str);
                    mMobRewardData.get(str).save(section);
                    n++;
                }

                if (n != 0) {
                    messages.debug("Saving Mobhunting extra MysteriousHalloween data.");
                    config.save(file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  void saveMysteriousHalloweenMobsData(String key) {
        try {
            if (mMobRewardData.containsKey(key)) {
                ConfigurationSection section = config.createSection(key);
                mMobRewardData.get(key).save(section);
                messages.debug("Saving extra MysteriousHalloweens data for mob=%s (%s)", key,
                        mMobRewardData.get(key).getMobName());
                config.save(file);
            } else {
                messages.debug("ERROR! MysteriousHalloween ID (%s) is not found in mMobRewardData", key);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // **************************************************************************
    // EVENTS
    // **************************************************************************

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onMysteriousHalloweenSpawnEvent(EntitySpawnEvent event) {

        Entity entity = event.getEntity();

        if (isMysteriousHalloween(entity)) {

            MobType monster = getMysteriousHalloweenType(entity);

            if (mMobRewardData != null && !mMobRewardData.containsKey(monster.name())) {
                messages.debug("New MysteriousHalloween mob found=%s (%s)", monster.name(), monster.toString());
                mMobRewardData.put(monster.name(),
                        new RewardData(MobPlugin.MysteriousHalloween, monster.name(),
                                MysteriousHalloweenAPI.getMobTypeName(monster), "40:60",
                                "minecraft:give {player} iron_sword 1", "You got an Iron sword.", 1, 1, 0.02));
                saveMysteriousHalloweenMobsData(monster.name());
                iDataStore.insertMysteriousHalloweenMobs(monster.name());
                // Update mob loaded into memory
                extendedMobManager.updateExtendedMobs();
                messages.injectMissingMobNamesToLangFiles();
            }

            event.getEntity().setMetadata(MH_MYSTERIOUSHALLOWEEN,
                    new FixedMetadataValue(mPlugin, mMobRewardData.get(monster.name())));
        }
    }

    public  int getProgressAchievementLevel1(String mobtype) {
        return mMobRewardData.get(mobtype).getAchivementLevel1();
    }

}
