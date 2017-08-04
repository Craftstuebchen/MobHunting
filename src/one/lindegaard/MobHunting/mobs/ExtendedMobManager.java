package one.lindegaard.MobHunting.mobs;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.compatibility.*;
import one.lindegaard.MobHunting.storage.DataStoreException;
import one.lindegaard.MobHunting.storage.IDataStore;
import one.lindegaard.MobHunting.util.Misc;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class ExtendedMobManager {

	private static HashMap<Integer, ExtendedMob> mobs = new HashMap<Integer, ExtendedMob>();

	private IDataStore iDataStore;
	private Messages messages;
	private CustomMobsCompat customMobsCompat;
	private TARDISWeepingAngelsCompat tARDISWeepingAngelsCompat;
	private MysteriousHalloweenCompat mysteriousHalloweenCompat;
    private MythicMobsCompat mythicMobsCompat;
    private CitizensCompat citizensCompat;

    public ExtendedMobManager(IDataStore iDataStore, Messages messages, CustomMobsCompat customMobsCompat,
                              TARDISWeepingAngelsCompat tARDISWeepingAngelsCompat, MysteriousHalloweenCompat mysteriousHalloweenCompat, MythicMobsCompat mythicMobsCompat, CitizensCompat citizensCompat) {
		this.iDataStore = iDataStore;
		this.messages = messages;
		this.customMobsCompat = customMobsCompat;
		this.tARDISWeepingAngelsCompat = tARDISWeepingAngelsCompat;
		this.mysteriousHalloweenCompat = mysteriousHalloweenCompat;
        this.mythicMobsCompat = mythicMobsCompat;
        this.citizensCompat = citizensCompat;
        updateExtendedMobs();
	}

	public void updateExtendedMobs() {
		iDataStore.insertMissingVanillaMobs();
		if (citizensCompat.isSupported())
			iDataStore.insertMissingCitizensMobs();
		if (mythicMobsCompat.isSupported())
			iDataStore.insertMissingMythicMobs();
		if (customMobsCompat.isSupported())
			iDataStore.insertCustomMobs();
		if (tARDISWeepingAngelsCompat.isSupported())
			iDataStore.insertTARDISWeepingAngelsMobs();
		if (mysteriousHalloweenCompat.isSupported())
			iDataStore.insertMysteriousHalloweenMobs();
		if (SmartGiantsCompat.isSupported())
			iDataStore.insertSmartGiants();
		// Not needed
		// if (InfernalMobsCompat.isSupported())
		// MobHunting.getStoreManager().insertInfernalMobs();

		Set<ExtendedMob> set = new HashSet<ExtendedMob>();

		try {
			set = iDataStore.loadMobs();
		} catch (DataStoreException e) {
			Bukkit.getLogger().severe("[MobHunting] Could not load data from mh_Mobs");
			e.printStackTrace();
		}

		int n = 0;
		Iterator<ExtendedMob> mobset = set.iterator();
		while (mobset.hasNext()) {
			ExtendedMob mob = mobset.next();
			switch (mob.getMobPlugin()) {
			case MythicMobs:
				if (!mythicMobsCompat.isSupported() || mythicMobsCompat.isDisabledInConfig()
						|| !mythicMobsCompat.isMythicMob(mob.getMobtype()))
					continue;
				break;

			case CustomMobs:
				if (!customMobsCompat.isSupported() || customMobsCompat.isDisabledInConfig())
					continue;
				break;

			case TARDISWeepingAngels:
				if (!tARDISWeepingAngelsCompat.isSupported() || tARDISWeepingAngelsCompat.isDisabledInConfig())
					continue;
				break;

			case Citizens:
				if (!citizensCompat.isSupported() || citizensCompat.isDisabledInConfig()
						|| !citizensCompat.isSentryOrSentinelOrSentries(mob.getMobtype()))
					continue;
				break;

			case MysteriousHalloween:
				if (!mysteriousHalloweenCompat.isSupported() || mysteriousHalloweenCompat.isDisabledInConfig())
					continue;
				break;

			case SmartGiants:
				if (!SmartGiantsCompat.isSupported() || SmartGiantsCompat.isDisabledInConfig())
					continue;
				break;

			case InfernalMobs:
				if (!InfernalMobsCompat.isSupported() || InfernalMobsCompat.isDisabledInConfig())
					continue;
				break;

			case Minecraft:
				break;

			default:
				ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
				console.sendMessage(ChatColor.RED + "[MobHunting] Missing PluginType: " + mob.getMobPlugin().getName()
						+ " in ExtendedMobManager.");
				continue;
			}
			if (!mobs.containsKey(mob.getMob_id())) {
				n++;
				mobs.put(mob.getMob_id(), mob);
			}
		}
		messages.debug("%s mobs was loaded into memory. Total mobs=%s", n, mobs.size());
	}

	public ExtendedMob getExtendedMobFromMobID(int i) {
		return mobs.get(i);
	}

	public HashMap<Integer, ExtendedMob> getAllMobs() {
		return mobs;
	}

	public int getMobIdFromMobTypeAndPluginID(String mobtype, MobPlugin mobPlugin) {
		Iterator<Entry<Integer, ExtendedMob>> mobset = mobs.entrySet().iterator();
		while (mobset.hasNext()) {
			ExtendedMob mob = mobset.next().getValue();
			if (mob.getMobPlugin().equals(mobPlugin) && mob.getMobtype().equalsIgnoreCase(mobtype))
				return mob.getMob_id();
		}
		// Bukkit.getLogger().warning("[MobHunting] The " + mobPlugin.name() + "
		// mobtype " + mobtype + " was not found.");
		return 0;
	}

	public ExtendedMob getExtendedMobFromEntity(Entity entity) {
		int mob_id;
		MobPlugin mobPlugin;
		String mobtype;

		if (mythicMobsCompat.isMythicMob(entity)) {
			mobPlugin = MobPlugin.MythicMobs;
			mobtype = mythicMobsCompat.getMythicMobType(entity);
		} else if (citizensCompat.isNPC(entity)) {
			mobPlugin = MobPlugin.Citizens;
			mobtype = String.valueOf(citizensCompat.getNPCId(entity));
		} else if (tARDISWeepingAngelsCompat.isWeepingAngelMonster(entity)) {
			mobPlugin = MobPlugin.TARDISWeepingAngels;
			mobtype = tARDISWeepingAngelsCompat.getWeepingAngelMonsterType(entity).name();
		} else if (customMobsCompat.isCustomMob(entity)) {
			mobPlugin = MobPlugin.CustomMobs;
			mobtype = customMobsCompat.getCustomMobType(entity);
		} else if (mysteriousHalloweenCompat.isMysteriousHalloween(entity)) {
			mobPlugin = MobPlugin.MysteriousHalloween;
			mobtype = mysteriousHalloweenCompat.getMysteriousHalloweenType(entity).name();
		} else if (SmartGiantsCompat.isSmartGiants(entity)) {
			mobPlugin = MobPlugin.SmartGiants;
			mobtype = SmartGiantsCompat.getSmartGiantsMobType(entity);
		} else if (InfernalMobsCompat.isInfernalMob(entity)) {
			mobPlugin = MobPlugin.InfernalMobs;
			MinecraftMob mob = MinecraftMob.getMinecraftMobType(entity);
			if (mob != null)
				mobtype = mob.name();
			else{
				messages.debug("unhandled entity %s", entity.getType());
				mobtype = "";
			}
		} else {
			// StatType
			mobPlugin = MobPlugin.Minecraft;
			MinecraftMob mob = MinecraftMob.getMinecraftMobType(entity);
			if (mob != null)
				mobtype = mob.name();
			else
				mobtype = "";
		}
		mob_id = getMobIdFromMobTypeAndPluginID(mobtype, mobPlugin);
		return new ExtendedMob(mob_id, mobPlugin, mobtype, customMobsCompat, messages, mythicMobsCompat, citizensCompat, tARDISWeepingAngelsCompat, mysteriousHalloweenCompat);
	}

	// This is only used to get a "random" mob_id stored when an Achievement is
	// stored in mh_Daily
	public ExtendedMob getFirstMob() {
		int mob_id = mobs.keySet().iterator().next();
		return mobs.get(mob_id);
	}

	public static String getMobName(Entity mob) {
		if (Misc.isMC18OrNewer())
			return mob.getName();
		else
			return mob.getType().toString();
	}

	public String getTranslatedName() {
		return "";
	}

}
