package one.lindegaard.MobHunting.npc;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.compatibility.CitizensCompat;
import one.lindegaard.MobHunting.util.Misc;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class MasterMobHunterManager implements Listener {

	private static HashMap<Integer, MasterMobHunter> mMasterMobHunter = new HashMap<Integer, MasterMobHunter>();

	private File file = new File(MobHunting.getInstance().getDataFolder(), "citizens-MasterMobHunter.yml");
	private YamlConfiguration config = new YamlConfiguration();

	private BukkitTask mUpdater = null;
	private CitizensCompat citizensCompat;
	private ConfigManager configManager;
	private Messages messages;

	public MasterMobHunterManager(CitizensCompat citizensCompat, ConfigManager configManager, Messages messages) {
		this.citizensCompat = citizensCompat;
		this.configManager = configManager;
        this.messages = messages;
    }

	public static HashMap<Integer, MasterMobHunter> getMasterMobHunterManager() {
		return mMasterMobHunter;
	}

	public void initialize() {
		if (citizensCompat.isSupported()) {
			loadData();
			Bukkit.getPluginManager().registerEvents(new MasterMobHunterTrait(), MobHunting.getInstance());
			Bukkit.getPluginManager().registerEvents(this, MobHunting.getInstance());
			Bukkit.getPluginManager().registerEvents(new MasterMobHunterSign(MobHunting.getInstance()),
					MobHunting.getInstance());
			mUpdater = Bukkit.getScheduler().runTaskTimer(MobHunting.getInstance(), new Updater(), 1L,
					configManager.masterMobHuntercheckEvery * 20);

		}
	}

	public void forceUpdate() {
		mUpdater = Bukkit.getScheduler().runTaskAsynchronously(MobHunting.getInstance(), new Updater());
	}

	public void update(NPC npc) {
		if (hasMasterMobHunterData(npc)) {
			MasterMobHunter mmh = new MasterMobHunter(npc, masterMobHunterSign);
            mmh.update();
            mMasterMobHunter.put(npc.getId(), mmh);
        }
	}

	public boolean isMasterMobHunter(Entity entity) {
		if (CitizensAPI.getNPCRegistry().isNPC(entity)) {
			NPC npc = citizensCompat.getNPC(entity);
			return (npc.hasTrait(MasterMobHunterTrait.class));
		} else
			return false;
	}

	public static boolean hasMasterMobHunterData(NPC npc) {
		return (npc.getTrait(MasterMobHunterTrait.class).stattype != null);
	}

	public MasterMobHunter get(int id) {
		return mMasterMobHunter.get(id);
	}

	public HashMap<Integer, MasterMobHunter> getAll() {
		return mMasterMobHunter;
	}

	public void put(int id, MasterMobHunter mmh) {
		mMasterMobHunter.put(id, mmh);
	}

	public boolean contains(int id) {
		return mMasterMobHunter.containsKey(id);
	}

	public void remove(int id) {
		mMasterMobHunter.remove(id);
	}

	public void shutdown() {
		if (mUpdater != null)
			mUpdater.cancel();
	}

	public void loadData() {
		try {
			if (!file.exists())
				return;
			config.load(file);
			int n = 0;
			for (String key : config.getKeys(false)) {
				ConfigurationSection section = config.getConfigurationSection(key);
				NPC npc = CitizensAPI.getNPCRegistry().getById(Integer.parseInt(key));
				if (npc != null && npc.hasTrait(MasterMobHunterTrait.class)) {
					MasterMobHunter mmh = new MasterMobHunter(npc, masterMobHunterSign);
					if (npc.getTrait(MasterMobHunterTrait.class).stattype == null) {
						mmh.read(section);
						n++;
						section.set(key, null);
						config.save(file);
					}
					mMasterMobHunter.put(Integer.valueOf(key), mmh);
					mmh.getHome();
				}
			}
			messages.debug("The file citizens-MasterMobHunter.yml is not used anymore and can be deleted.");
			if (n > 0)
				messages.debug("Loaded %s MasterMobHunter Traits's from file.", n);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public static boolean isMasterMobHunter(NPC npc) {
		return (npc.hasTrait(MasterMobHunterTrait.class));
	}

	// ****************************************************************************
	// Save & Load
	// ****************************************************************************

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onClick(NPCLeftClickEvent event) {
		messages.debug("NPCLeftClickEvent");
		NPC npc = event.getNPC();
		if (isMasterMobHunter(npc)) {
			@SuppressWarnings("deprecation")
			ItemStack is = event.getClicker().getItemInHand();
			// Messages.debug("ItemStack=%s", is);
			if (!is.getType().equals(Material.STICK)) {
				if (Misc.isMC110OrNewer()) {
					// ((Player) npc).getInventory().setItemInMainHand(is);
					// ((Player)
					// event.getClicker()).getInventory().setItemInMainHand(new
					// ItemStack(Material.AIR));
				} else {
					// ((Player) npc).getInventory().setItemInHand(is);
					// ((Player)
					// event.getClicker()).getInventory().setItemInHand(new
					// ItemStack(Material.AIR));
				}
				Trait trait = getSentinelOrSentryTrait(npc);
				if (trait != null) {
					trait.getNPC().faceLocation(event.getClicker().getLocation());
					trait.getNPC().getDefaultSpeechController()
							.speak(new SpeechContext("Don't hit me!!!", event.getClicker()));
					trait.getNPC().getNavigator().setTarget(event.getClicker(), true);
				}
			} else {
				npc.setName("UPDATING SKIN");
				update(npc);
				MasterMobHunter mmh = mMasterMobHunter.get(npc.getId());
				mmh.update();
				messages.playerActionBarMessage(event.getClicker(),
                        messages.getString("mobhunting.npc.clickednpc", "killer",
								CitizensAPI.getNPCRegistry().getById(npc.getId()).getName(), "rank", mmh.getRank(),
								"numberofkills", mmh.getNumberOfKills(), "stattype", mmh.getStatType().translateName(),
								"period", mmh.getPeriod().translateNameFriendly(), "npcid", npc.getId()));
				mMasterMobHunter.put(event.getNPC().getId(), mmh);
			}
		}
	}

	// ****************************************************************************
	// Events
	// ****************************************************************************

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onKilledTarget(EntityDeathEvent event) {
		if (isMasterMobHunter(event.getEntity().getKiller()) && event.getEntity() instanceof Player) {
			NPC npc = citizensCompat.getNPC(event.getEntity().getKiller());
			final Player player = (Player) event.getEntity();
			final NPC npc1 = npc;
			Bukkit.getScheduler().runTaskLaterAsynchronously(MobHunting.getInstance(), new Runnable() {
				public void run() {
                    messages.debug("NPC %s (ID=%s) killed %s - return to home", npc1.getName(), npc1.getId(),
							player.getName());
					npc1.teleport(mMasterMobHunter.get(npc1.getId()).getHome(), TeleportCause.PLUGIN);
				}
			}, 20 * 10); // 20ticks/sec * 10 sec
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onSpawnNPC(NPCSpawnEvent event) {
		NPC npc = event.getNPC();
		if (isMasterMobHunter(npc)) {
			if (npc.getStoredLocation() != null && mMasterMobHunter.containsKey(npc.getId())
					&& npc.getStoredLocation().distance(mMasterMobHunter.get(npc.getId()).getHome()) > 0.2) {
                messages.debug("NPC %s (ID=%s) return to home", npc.getName(), npc.getId());
				final NPC npc1 = npc;
				Bukkit.getScheduler().runTaskLaterAsynchronously(MobHunting.getInstance(), new Runnable() {
					public void run() {
						npc1.teleport(mMasterMobHunter.get(npc1.getId()).getHome(), TeleportCause.PLUGIN);
					}
				}, 20 * 10); // 20ticks/sec * 10 sec
			}
		}
	}

	private Trait getSentinelOrSentryTrait(NPC npc) {
		Trait trait = null;
		if (citizensCompat.isSentryOrSentinelOrSentries(npc.getEntity())) {
			if (npc.hasTrait(CitizensAPI.getTraitFactory().getTraitClass("Sentinel")))
				trait = npc.getTrait(CitizensAPI.getTraitFactory().getTraitClass("Sentinel"));
			else if (npc.hasTrait(CitizensAPI.getTraitFactory().getTraitClass("Sentry")))
				trait = npc.getTrait(CitizensAPI.getTraitFactory().getTraitClass("Sentry"));

		} else {// how to handle/add Trait ???
			// npc.addTrait(CitizensAPI.getTraitFactory().getTraitClass("Sentinel"));
			// trait =
			// npc.getTrait(CitizensAPI.getTraitFactory().getTraitClass("Sentinel"));
			// Messages.debug("Sentinel trait added to %s (id=%s)",
			// npc.getName(), npc.getId());
		}
		return trait;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onClick(NPCRightClickEvent event) {
		NPC npc = event.getNPC();
		if (isMasterMobHunter(npc)) {
			update(npc);
			MasterMobHunter mmh = mMasterMobHunter.get(npc.getId());
			mmh.update();
            messages.playerActionBarMessage(event.getClicker(),
                    messages.getString("mobhunting.npc.clickednpc", "killer",
							CitizensAPI.getNPCRegistry().getById(npc.getId()).getName(), "rank", mmh.getRank(),
							"numberofkills", mmh.getNumberOfKills(), "stattype", mmh.getStatType().translateName(),
							"period", mmh.getPeriod().translateNameFriendly(), "npcid", npc.getId()));
			mMasterMobHunter.put(event.getNPC().getId(), mmh);
		}
	}

	private class Updater implements Runnable {
		@Override
		public void run() {
			if (citizensCompat.isSupported()) {
				int n = 0;
				for (Iterator<NPC> npcList = CitizensAPI.getNPCRegistry().iterator(); npcList.hasNext();) {
					NPC npc = npcList.next();
					if (isMasterMobHunter(npc.getEntity())) {
						update(npc);
						n++;
					}
				}
				if (n > 0)
					messages.debug("Refreshed %s MasterMobHunters", n);
			} else {
				messages.debug("MasterMobHunterManager: Citizens is disabled.");
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onPlayerJoin(PlayerJoinEvent event) {
		Iterator<NPC> itr = CitizensAPI.getNPCRegistry().iterator();
		while (itr.hasNext()) {
			NPC npc = itr.next();
			if (event.getPlayer().getName().equals(npc.getName()) && isMasterMobHunter(npc))
				update(npc);
		}
	}

	// @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	// public void onSpawnNPC(NPCTraitEvent event) {
	// Messages.debug("NPCTraitEvent NPC=%s, Trait=%s", event.getNPC().getId(),
	// event.getTrait().getName());
	// }
}
