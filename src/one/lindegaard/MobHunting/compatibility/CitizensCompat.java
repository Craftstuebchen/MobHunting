package one.lindegaard.MobHunting.compatibility;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.CitizensPlugin;
import net.citizensnpcs.api.event.CitizensDisableEvent;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCDamageEvent;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.event.PlayerCreateNPCEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.TraitInfo;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.mobs.MobPlugin;
import one.lindegaard.MobHunting.npc.MasterMobHunter;
import one.lindegaard.MobHunting.npc.MasterMobHunterManager;
import one.lindegaard.MobHunting.npc.MasterMobHunterTrait;
import one.lindegaard.MobHunting.rewards.RewardData;
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

public class CitizensCompat implements Listener {

	private static boolean supported = false;
	private static CitizensPlugin citizensAPI;
	private static HashMap<String, RewardData> mMobRewardData = new HashMap<String, RewardData>();
	private static File fileMobRewardData = new File(MobHunting.getInstance().getDataFolder(), "citizens-rewards.yml");
	private static YamlConfiguration config = new YamlConfiguration();
	public static final String MH_CITIZENS = "MH:CITIZENS";

	private static MasterMobHunterManager masterMobHunterManager = new MasterMobHunterManager();

	public CitizensCompat() {
		initialize();
	}

	private void initialize() {
		if (isDisabledInConfig()) {
			Bukkit.getLogger().info("[MobHunting] Compatibility with Citizens2 is disabled in config.yml");
		} else {
			citizensAPI = (CitizensPlugin) Bukkit.getPluginManager().getPlugin("Citizens");
			if (citizensAPI == null)
				return;

			TraitInfo trait = TraitInfo.create(MasterMobHunterTrait.class).withName("MasterMobHunter");
			citizensAPI.getTraitFactory().registerTrait(trait);

			Bukkit.getLogger().info("[MobHunting] Enabling compatibility with Citizens ("
					+ getCitizensPlugin().getDescription().getVersion() + ")");

			supported = true;

			// wait x seconds or until Citizens is fully loaded.
			Bukkit.getScheduler().runTaskLaterAsynchronously(MobHunting.getInstance(), new Runnable() {
				public void run() {
					loadCitizensData();
					saveCitizensData();

					masterMobHunterManager.initialize();
					findMissingNPCs();
					MobHunting.getExtendedMobManager().updateExtendedMobs();
					Messages.injectMissingMobNamesToLangFiles();
				}
			}, 20 * 5); // 20ticks/sec * 3 sec.

		}
	}

	// **************************************************************************
	// LOAD & SAVE
	// **************************************************************************
	public static void loadCitizensData() {
		try {
			if (!fileMobRewardData.exists())
				return;
			Messages.debug("Loading extra MobRewards for Citizens NPC.");

			config.load(fileMobRewardData);
			int n = 0;
			for (String key : config.getKeys(false)) {
				if (isNPC(Integer.valueOf(key))) {
					ConfigurationSection section = config.getConfigurationSection(key);
					RewardData mrd = new RewardData();
					mrd.read(section);
					mMobRewardData.put(key, mrd);
					MobHunting.getStoreManager().insertCitizensMobs(key);
					n++;
				} else {
					Messages.debug("The mob=%s can't be found in Citizens saves.yml file", key);
				}
			}
			Messages.debug("Loaded %s extra MobRewards.", n);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}

	}

	public static void saveCitizensData() {
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
					Messages.debug("Saving %s MobRewards to file.", mMobRewardData.size());
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
				Messages.debug("Saving Sentry/Sentinel Trait Reward data for ID=%s.", key);
				config.save(fileMobRewardData);
			} else {
				Messages.debug("ERROR! Sentry/Sentinel ID (%s) is not found in mMobRewardData", key);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// **************************************************************************
	// OTHER FUNCTIONS
	// **************************************************************************
	public static void shutdown() {
		if (supported) {
			masterMobHunterManager.shutdown();
			TraitInfo trait = TraitInfo.create(MasterMobHunterTrait.class).withName("MasterMobHunter");
			if (Misc.isMC18OrNewer())
				citizensAPI.getTraitFactory().deregisterTrait(trait);
		}
	}

	public static CitizensPlugin getCitizensPlugin() {
		return citizensAPI;
	}

	public static boolean isSupported() {
		if (supported && citizensAPI != null && CitizensAPI.hasImplementation())
			return supported;
		else
			return false;
	}

	public static MasterMobHunterManager getManager() {
		return masterMobHunterManager;
	}

	public static boolean isNPC(Entity entity) {
		if (isSupported())
			return CitizensAPI.getNPCRegistry().isNPC(entity);
		return false;
	}

	public static boolean isNPC(Integer id) {
		if (isSupported())
			return CitizensAPI.getNPCRegistry().getById(id) != null;
		return false;
	}

	public static int getNPCId(Entity entity) {
		return CitizensAPI.getNPCRegistry().getNPC(entity).getId();
	}

	public static String getNPCName(Entity entity) {
		return CitizensAPI.getNPCRegistry().getNPC(entity).getName();
	}

	public static NPC getNPC(Entity entity) {
		return CitizensAPI.getNPCRegistry().getNPC(entity);
	}

	public static boolean isSentryOrSentinelOrSentries(String mobtype) {
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

	public static boolean isSentryOrSentinelOrSentries(Entity entity) {
		if (isNPC(entity))
			return CitizensAPI.getNPCRegistry().getNPC(entity)
					.hasTrait(CitizensAPI.getTraitFactory().getTraitClass("Sentry"))
					|| CitizensAPI.getNPCRegistry().getNPC(entity)
							.hasTrait(CitizensAPI.getTraitFactory().getTraitClass("Sentinel"))
					|| CitizensAPI.getNPCRegistry().getNPC(entity)
							.hasTrait(CitizensAPI.getTraitFactory().getTraitClass("Sentries"));
		return false;
	}

	public static HashMap<String, RewardData> getMobRewardData() {
		return mMobRewardData;
	}

	public static boolean isDisabledInConfig() {
		return MobHunting.getConfigManager().disableIntegrationCitizens;
	}

	public static boolean isEnabledInConfig() {
		return !MobHunting.getConfigManager().disableIntegrationCitizens;
	}

	private void findMissingNPCs() {
		NPCRegistry n = CitizensAPI.getNPCRegistry();
		for (Iterator<NPC> npcList = n.iterator(); npcList.hasNext();) {
			NPC npc = npcList.next();
			if (isSentryOrSentinelOrSentries(npc.getEntity())) {
				if (mMobRewardData != null && !mMobRewardData.containsKey(String.valueOf(npc.getId()))) {
					Messages.debug("A new Sentinel or Sentry NPC was found. ID=%s,%s", npc.getId(), npc.getName());
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
		Messages.debug("onCitizensEnableEvent:%s", event.getEventName());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onCitizensDisableEvent(CitizensDisableEvent event) {
		// Messages.debug("CitizensDisableEvent - saving");
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onNPCSpawnEvent(NPCSpawnEvent event) {
		NPC npc = event.getNPC();
		if (npc.getId() == event.getNPC().getId()) {
			Messages.debug("NPCSpawnEvent: %s spawned", npc.getName());
			if (isSentryOrSentinelOrSentries(npc.getEntity())) {
				if (mMobRewardData != null && !mMobRewardData.containsKey(String.valueOf(npc.getId()))) {
					Messages.debug("A new Sentinel or Sentry NPC was found. ID=%s,%s", npc.getId(), npc.getName());
					// Update Reward data in memory
					mMobRewardData.put(String.valueOf(npc.getId()),
							new RewardData(MobPlugin.Citizens, "npc", npc.getFullName(), "0",
									"give {player} iron_sword 1", "You got an Iron sword.", 0, 1, 0.02));
					// Save Reward Data to disk
					saveCitizensData(String.valueOf(npc.getId()));
					// Insert new mob to Database
					MobHunting.getStoreManager().insertCitizensMobs(String.valueOf(npc.getId()));
					// Update mob loaded into memory
					MobHunting.getExtendedMobManager().updateExtendedMobs();
					Messages.injectMissingMobNamesToLangFiles();
				}
				npc.getEntity().setMetadata(MH_CITIZENS, new FixedMetadataValue(MobHunting.getInstance(),
						mMobRewardData.get(String.valueOf(npc.getId()))));
			}
			if (masterMobHunterManager.isMasterMobHunter(npc.getEntity())) {
				if (!masterMobHunterManager.contains(npc.getId())) {
					Messages.debug("A New MasterMobHunter NPC was found. ID=%s,%s", npc.getId(), npc.getName());
					masterMobHunterManager.put(npc.getId(), new MasterMobHunter(npc));
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onNPCDespawnEvent(NPCDespawnEvent event) {
		// Messages.debug("NPCDespawnEvent");
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onPlayerCreateNPCEvent(PlayerCreateNPCEvent event) {
		// Messages.debug("NPCCreateNPCEvent");
	}

	public static int getProgressAchievementLevel1(String mobtype) {
		return mMobRewardData.get(mobtype).getAchivementLevel1();
	}

}
