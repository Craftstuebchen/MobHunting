package one.lindegaard.MobHunting.compatibility;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.CitizensPlugin;
import net.citizensnpcs.api.event.*;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.TraitInfo;
import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import one.lindegaard.MobHunting.mobs.MobPlugin;
import one.lindegaard.MobHunting.npc.MasterMobHunter;
import one.lindegaard.MobHunting.npc.MasterMobHunterManager;
import one.lindegaard.MobHunting.npc.MasterMobHunterTrait;
import one.lindegaard.MobHunting.rewards.RewardData;
import one.lindegaard.MobHunting.storage.IDataStore;
import one.lindegaard.MobHunting.util.Misc;
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
import java.util.Iterator;

public class CitizensCompat implements Listener {

    public static final String MH_CITIZENS = "MH:CITIZENS";
    private static MasterMobHunterManager masterMobHunterManager = new MasterMobHunterManager(citizensCompat, configManager, messages);
    private boolean supported = false;
    private CitizensPlugin citizensAPI;
    private HashMap<String, RewardData> mMobRewardData = new HashMap<String, RewardData>();
    private File fileMobRewardData = new File(MobHunting.getInstance().getDataFolder(), "citizens-rewards.yml");
    private YamlConfiguration config = new YamlConfiguration();
    private Messages messages;
    private ExtendedMobManager extendedMobManager;
    private IDataStore iDataStore;
    private ConfigManager configManager;

    public CitizensCompat(Messages messages, ExtendedMobManager extendedMobManager, IDataStore iDataStore, ConfigManager configManager) {
        this.messages = messages;
        this.extendedMobManager = extendedMobManager;
        this.iDataStore = iDataStore;
        this.configManager = configManager;
        initialize();
    }

    private void initialize() {
        if (isDisabledInConfig()) {
            Bukkit.getConsoleSender()
                    .sendMessage("[MobHunting] Compatibility with Citizens2 is disabled in config.yml");
        } else {
            citizensAPI = (CitizensPlugin) Bukkit.getPluginManager().getPlugin("Citizens");
            if (citizensAPI == null)
                return;

            TraitInfo trait = TraitInfo.create(MasterMobHunterTrait.class).withName("MasterMobHunter");
            citizensAPI.getTraitFactory().registerTrait(trait);

            Bukkit.getConsoleSender().sendMessage("[MobHunting] Enabling compatibility with Citizens ("
                    + getCitizensPlugin().getDescription().getVersion() + ")");

            supported = true;

            // wait x seconds or until Citizens is fully loaded.
            Bukkit.getScheduler().runTaskLaterAsynchronously(MobHunting.getInstance(), new Runnable() {
                public void run() {
                    loadCitizensData();
                    saveCitizensData();

                    masterMobHunterManager.initialize();
                    findMissingNPCs();
                    extendedMobManager.updateExtendedMobs();
                    messages.injectMissingMobNamesToLangFiles();
                }
            }, 20 * 5); // 20ticks/sec * 3 sec.

        }
    }

    // **************************************************************************
    // LOAD & SAVE
    // **************************************************************************
    public void loadCitizensData() {
        try {
            if (!fileMobRewardData.exists())
                return;
            messages.debug("Loading extra MobRewards for Citizens NPC.");

            config.load(fileMobRewardData);
            int n = 0;
            for (String key : config.getKeys(false)) {
                if (isNPC(Integer.valueOf(key))) {
                    ConfigurationSection section = config.getConfigurationSection(key);
                    RewardData mrd = new RewardData();
                    mrd.read(section);
                    mMobRewardData.put(key, mrd);
                    iDataStore.insertCitizensMobs(key);
                    n++;
                } else {
                    messages.debug("The mob=%s can't be found in Citizens saves.yml file", key);
                }
            }
            messages.debug("Loaded %s extra MobRewards.", n);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

    }

    public void saveCitizensData() {
        try {
            config.options().header("This a extra MobHunting config data for the Citizens/NPC's on your server.");

            if (mMobRewardData.size() > 0) {

                int n = 0;
                for (String key : mMobRewardData.keySet()) {
                    ConfigurationSection section = config.createSection(key);
                    mMobRewardData.get(key).save(section);
                    n++;
                }

                if (n != 0) {
                    messages.debug("Saving %s MobRewards to file.", mMobRewardData.size());
                    config.save(fileMobRewardData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveCitizensData(String key) {
        try {
            if (mMobRewardData.containsKey(key)) {
                ConfigurationSection section = config.createSection(key);
                mMobRewardData.get(key).save(section);
                messages.debug("Saving Sentry/Sentinel Trait Reward data for ID=%s.", key);
                config.save(fileMobRewardData);
            } else {
                messages.debug("ERROR! Sentry/Sentinel ID (%s) is not found in mMobRewardData", key);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // **************************************************************************
    // OTHER FUNCTIONS
    // **************************************************************************
    public void shutdown() {
        if (supported) {
            masterMobHunterManager.shutdown();
            TraitInfo trait = TraitInfo.create(MasterMobHunterTrait.class).withName("MasterMobHunter");
            if (Misc.isMC18OrNewer())
                citizensAPI.getTraitFactory().deregisterTrait(trait);
        }
    }

    public CitizensPlugin getCitizensPlugin() {
        return citizensAPI;
    }

    public boolean isSupported() {
        if (supported && citizensAPI != null && CitizensAPI.hasImplementation())
            return supported;
        else
            return false;
    }

    public MasterMobHunterManager getManager() {
        return masterMobHunterManager;
    }

    public boolean isNPC(Entity entity) {
        if (isSupported())
            return CitizensAPI.getNPCRegistry().isNPC(entity);
        return false;
    }

    public boolean isNPC(Integer id) {
        if (isSupported())
            return CitizensAPI.getNPCRegistry().getById(id) != null;
        return false;
    }

    public int getNPCId(Entity entity) {
        return CitizensAPI.getNPCRegistry().getNPC(entity).getId();
    }

    public String getNPCName(Entity entity) {
        return CitizensAPI.getNPCRegistry().getNPC(entity).getName();
    }

    public NPC getNPC(Entity entity) {
        return CitizensAPI.getNPCRegistry().getNPC(entity);
    }

    public boolean isSentryOrSentinelOrSentries(String mobtype) {
        if (isNPC(Integer.valueOf(mobtype)))
            return CitizensAPI.getNPCRegistry().getById(Integer.valueOf(mobtype))
                    .hasTrait(CitizensAPI.getTraitFactory().getTraitClass("Sentry"))
                    || CitizensAPI.getNPCRegistry().getById(Integer.valueOf(mobtype))
                    .hasTrait(CitizensAPI.getTraitFactory().getTraitClass("Sentinel"))
                    || CitizensAPI.getNPCRegistry().getById(Integer.valueOf(mobtype))
                    .hasTrait(CitizensAPI.getTraitFactory().getTraitClass("Sentries"));
        else
            return false;
    }

    public boolean isSentryOrSentinelOrSentries(Entity entity) {
        if (isNPC(entity))
            return CitizensAPI.getNPCRegistry().getNPC(entity)
                    .hasTrait(CitizensAPI.getTraitFactory().getTraitClass("Sentry"))
                    || CitizensAPI.getNPCRegistry().getNPC(entity)
                    .hasTrait(CitizensAPI.getTraitFactory().getTraitClass("Sentinel"))
                    || CitizensAPI.getNPCRegistry().getNPC(entity)
                    .hasTrait(CitizensAPI.getTraitFactory().getTraitClass("Sentries"));
        return false;
    }

    public HashMap<String, RewardData> getMobRewardData() {
        return mMobRewardData;
    }

    public boolean isDisabledInConfig() {
        return configManager.disableIntegrationCitizens;
    }

    public boolean isEnabledInConfig() {
        return !isDisabledInConfig();
    }

    private void findMissingNPCs() {
        NPCRegistry n = CitizensAPI.getNPCRegistry();
        for (Iterator<NPC> npcList = n.iterator(); npcList.hasNext(); ) {
            NPC npc = npcList.next();
            if (isSentryOrSentinelOrSentries(npc.getEntity())) {
                if (mMobRewardData != null && !mMobRewardData.containsKey(String.valueOf(npc.getId()))) {
                    messages.debug("A new Sentinel or Sentry NPC was found. ID=%s,%s", npc.getId(), npc.getName());
                    mMobRewardData.put(String.valueOf(npc.getId()),
                            new RewardData(MobPlugin.Citizens, "npc", npc.getFullName(), "10",
                                    "give {player} iron_sword 1", "You got an Iron sword.", 1, 1, 0.02));
                    saveCitizensData(String.valueOf(npc.getId()));
                }
            }
        }
    }

    // **************************************************************************
    // EVENTS
    // **************************************************************************
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onNPCDeathEvent(NPCDeathEvent event) {

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onNPCDamageEvent(NPCDamageEvent event) {

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onNPCDamageByEntityEvent(NPCDamageByEntityEvent event) {

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onCitizensEnableEvent(CitizensEnableEvent event) {
        messages.debug("onCitizensEnableEvent:%s", event.getEventName());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onCitizensDisableEvent(CitizensDisableEvent event) {
        // messages.debug("CitizensDisableEvent - saving");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onNPCSpawnEvent(NPCSpawnEvent event) {
        NPC npc = event.getNPC();
        if (npc.getId() == event.getNPC().getId()) {
            messages.debug("NPCSpawnEvent: %s spawned", npc.getName());
            if (isSentryOrSentinelOrSentries(npc.getEntity())) {
                if (mMobRewardData != null && !mMobRewardData.containsKey(String.valueOf(npc.getId()))) {
                    messages.debug("A new Sentinel or Sentry NPC was found. ID=%s,%s", npc.getId(), npc.getName());
                    // Update Reward data in memory
                    mMobRewardData.put(String.valueOf(npc.getId()),
                            new RewardData(MobPlugin.Citizens, "npc", npc.getFullName(), "0",
                                    "give {player} iron_sword 1", "You got an Iron sword.", 0, 1, 0.02));
                    // Save Reward Data to disk
                    saveCitizensData(String.valueOf(npc.getId()));
                    // Insert new mob to Database
                    iDataStore.insertCitizensMobs(String.valueOf(npc.getId()));
                    // Update mob loaded into memory
                    extendedMobManager.updateExtendedMobs();
                    messages.injectMissingMobNamesToLangFiles();
                }
                npc.getEntity().setMetadata(MH_CITIZENS, new FixedMetadataValue(MobHunting.getInstance(),
                        mMobRewardData.get(String.valueOf(npc.getId()))));
            }
            if (masterMobHunterManager.isMasterMobHunter(npc.getEntity())) {
                if (!masterMobHunterManager.contains(npc.getId())) {
                    messages.debug("A New MasterMobHunter NPC was found. ID=%s,%s", npc.getId(), npc.getName());
                    masterMobHunterManager.put(npc.getId(), new MasterMobHunter(npc, masterMobHunterSign));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onNPCDespawnEvent(NPCDespawnEvent event) {
        // messages.debug("NPCDespawnEvent");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerCreateNPCEvent(PlayerCreateNPCEvent event) {
        // messages.debug("NPCCreateNPCEvent");
    }

    public int getProgressAchievementLevel1(String mobtype) {
        return mMobRewardData.get(mobtype).getAchivementLevel1();
    }

}
