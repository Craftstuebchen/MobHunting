package one.lindegaard.MobHunting.compatibility;

import de.hellfirepvp.api.CustomMobsAPI;
import de.hellfirepvp.api.data.ICustomMob;
import de.hellfirepvp.api.event.CustomMobDeathEvent;
import de.hellfirepvp.api.event.CustomMobSpawnEvent;
import de.hellfirepvp.api.event.CustomMobSpawnEvent.SpawnReason;
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
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class CustomMobsCompat implements Listener {

    // https://www.spigotmc.org/resources/custommobs.7339/

    public static final String MH_CUSTOMMOBS = "MH:CUSTOMMOBS";
    private static boolean supported = false;
    private static Plugin mPlugin;
    private static HashMap<String, RewardData> mMobRewardData = new HashMap<String, RewardData>();
    private static File file = new File(MobHunting.getInstance().getDataFolder(), "custommobs-rewards.yml");
    private static YamlConfiguration config = new YamlConfiguration();
    private IDataStore iDataStore;
    private ConfigManager configManager;
    private ExtendedMobManager extendedMobManager;
    private MobHunting mobHunting;
    private Messages messages;

    public CustomMobsCompat(MobHunting mobHunting) {
        this.mobHunting=mobHunting;
        this.iDataStore = mobHunting.getStoreManager();
        this.configManager = mobHunting.getConfigManager();
        this.extendedMobManager = mobHunting.getExtendedMobManager();
        this.messages= mobHunting.getMessages();

        if (isDisabledInConfig()) {
            Bukkit.getConsoleSender()
                    .sendMessage("[MobHunting] Compatibility with CustomMobs is disabled in config.yml");
        } else {
            mPlugin = Bukkit.getPluginManager().getPlugin("CustomMobs");

            Bukkit.getPluginManager().registerEvents(this, MobHunting.getInstance());

            Bukkit.getConsoleSender().sendMessage("[MobHunting] Enabling Compatibility with CustomMobs ("
                    + getCustomMobs().getDescription().getVersion() + ")");

            supported = true;

            loadCustomMobsData();
            saveCustomMobsData();
        }
    }

    // **************************************************************************
    // LOAD & SAVE
    // **************************************************************************
    public void loadCustomMobsData() {
        try {
            if (!file.exists())
                return;

            config.load(file);
            for (String key : CustomMobsAPI.getKnownMobTypes()) {
                if (config.contains(key)) {
                    ConfigurationSection section = config.getConfigurationSection(key);
                    RewardData mob = new RewardData();
                    if (isCustomMob(key)) {
                        mob.read(section);
                        mob.setMobType(key);
                        if (mob.getMobName() == null)
                            mob.setMobName(mob.getMobType());
                    } else
                        mob = new RewardData(MobPlugin.CustomMobs, CustomMobsAPI.getCustomMob(key).getName(),
                                CustomMobsAPI.getCustomMob(key).getDisplayName(), "10",
                                "minecraft:give {player} iron_sword 1", "You got an Iron sword.", 1, 1, 0.02);

                    mMobRewardData.put(key, mob);
                    iDataStore.insertCustomMobs(key);
                }
            }
            messages.injectMissingMobNamesToLangFiles();
            messages.debug("Loaded %s CustomMobs", mMobRewardData.size());
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void loadCustomMobsData(String key) {
        try {
            if (!file.exists())
                return;

            config.load(file);
            ConfigurationSection section = config.getConfigurationSection(key);
            if (isCustomMob(key)) {
                RewardData mob = new RewardData();
                mob.read(section);
                mob.setMobType(key);
                if (mob.getMobName() == null)
                    mob.setMobName(mob.getMobType());
                mMobRewardData.put(key, mob);
                iDataStore.insertCustomMobs(key);
            } else {
                messages.debug("The mob=%s cant be found in CustomMobs configuration file", key);
            }
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void saveCustomMobsData() {
        try {
            config.options().header("This a extra MobHunting config data for the CustomMobs on your server.");

            if (mMobRewardData.size() > 0) {

                int n = 0;
                for (String str : mMobRewardData.keySet()) {
                    ConfigurationSection section = config.createSection(str);
                    mMobRewardData.get(str).save(section);
                    n++;
                }

                if (n != 0) {
                    messages.debug("Saving Mobhunting extra CustomMobs data.");
                    config.save(file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveCustomMobsData(String key) {
        try {
            if (mMobRewardData.containsKey(key)) {
                ConfigurationSection section = config.createSection(key);
                mMobRewardData.get(key).save(section);
                messages.debug("Saving Mobhunting extra CustomMobs data.");
                config.save(file);
            } else {
                messages.debug("ERROR! CustomMobs ID (%s) is not found in mMobRewardData", key);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // **************************************************************************
    // OTHER FUNCTIONS
    // **************************************************************************
    public Plugin getCustomMobs() {
        return mPlugin;
    }

    public boolean isSupported() {
        return supported;
    }

    public boolean isCustomMob(Entity entity) {
        if (isSupported())
            return entity.hasMetadata(MH_CUSTOMMOBS);
        return false;
    }

    public boolean isCustomMob(String mob) {
        if (isSupported())
            return CustomMobsAPI.getCustomMob(mob) != null;
        return false;
    }

    public String getCustomMobType(Entity killed) {
        if (killed.hasMetadata(MH_CUSTOMMOBS)) {
            List<MetadataValue> data = killed.getMetadata(MH_CUSTOMMOBS);
            MetadataValue value = data.get(0);
            return ((RewardData) value.value()).getMobType();
        } else
            return "";
    }

    public HashMap<String, RewardData> getMobRewardData() {
        return mMobRewardData;
    }

    public boolean isDisabledInConfig() {
        return configManager.disableIntegrationCustomMobs;
    }

    public boolean isEnabledInConfig() {
        return !configManager.disableIntegrationCustomMobs;
    }

    // **************************************************************************
    // EVENTS
    // **************************************************************************
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onCustomMobDeathEvent(CustomMobDeathEvent event) {
        // ICustomMob mob = event.getMob(); // The ICustomMob that was killed.
        // Player killer = event.getKiller(); // The Player that killed the
        // // CustomMob;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onCustomMobSpawnEvent(CustomMobSpawnEvent event) {
        // The ICustomMob that spawned.
        ICustomMob mob = event.getMob();

        // The LivingEntity instance that was spawned.
        LivingEntity entity = event.getEntity();

        messages.debug("CustomMobSpawnEvent: MinecraftMobtype=%s CustomMobName=%s", entity.getType(), mob.getName());

        // Specific reason why the mob was spawned
        CustomMobSpawnEvent.SpawnReason reason = event.getReason();
        if (reason.equals(SpawnReason.SPAWNER) && !configManager.allowCustomMobsSpawners) {
            entity.setMetadata("MH:blocked", new FixedMetadataValue(mobHunting, true));
            // Block spawner = event.getSpawner();
            // Is only defined when the spawnReason is SPAWNER.
        }

        if (mMobRewardData != null && !mMobRewardData.containsKey(mob.getName())) {
            messages.debug("New CustomMobName found=%s,%s", mob.getName(), mob.getDisplayName());
            String name = mob.getDisplayName() == null ? mob.getName() : mob.getDisplayName();
            mMobRewardData.put(mob.getName(), new RewardData(MobPlugin.CustomMobs, mob.getName(), name, "10",
                    "minecraft:give {player} iron_sword 1", "You got an Iron sword.", 1, 1, 0.02));
            saveCustomMobsData(mob.getName());
            iDataStore.insertCustomMobs(mob.getName());
            // Update mob loaded into memory
            extendedMobManager.updateExtendedMobs();
            messages.injectMissingMobNamesToLangFiles();
        }

        entity.setMetadata(MH_CUSTOMMOBS,
                new FixedMetadataValue(mobHunting, mMobRewardData.get(mob.getName())));
    }

    public int getProgressAchievementLevel1(String mobtype) {
        return mMobRewardData.get(mobtype).getAchivementLevel1();
    }

}
