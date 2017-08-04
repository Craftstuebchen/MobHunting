package one.lindegaard.MobHunting.mobs;

import net.citizensnpcs.api.npc.NPC;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.compatibility.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class ExtendedMob {

	private Integer mob_id; // The unique mob_id from mh_Mobs
	private MobPlugin mobPlugin; // Plugin_id from mh_Plugins
	private String mobtype; // mobtype NOT unique
	private CustomMobsCompat customMobsCompat;
	private Messages messages;
	private MythicMobsCompat mythicMobsCompat;
	private CitizensCompat citizensCompat;
	private TARDISWeepingAngelsCompat tARDISWeepingAngelsCompat;
	private MysteriousHalloweenCompat mysteriousHalloweenCompat;

	public ExtendedMob(Integer mob_id, MobPlugin mobPlugin, String mobtype, CustomMobsCompat customMobsCompat, Messages messages, MythicMobsCompat mythicMobsCompat, CitizensCompat citizensCompat, TARDISWeepingAngelsCompat tARDISWeepingAngelsCompat, MysteriousHalloweenCompat mysteriousHalloweenCompat) {
		this.mob_id = mob_id;
		this.mobPlugin = mobPlugin;
		this.mobtype = mobtype;
		this.customMobsCompat = customMobsCompat;
		this.messages = messages;
		this.mythicMobsCompat = mythicMobsCompat;
		this.citizensCompat = citizensCompat;
		this.tARDISWeepingAngelsCompat = tARDISWeepingAngelsCompat;
		this.mysteriousHalloweenCompat = mysteriousHalloweenCompat;
	}

	public ExtendedMob(MobPlugin mobPlugin, String mobtype, CustomMobsCompat customMobsCompat, Messages messages, MythicMobsCompat mythicMobsCompat, CitizensCompat citizensCompat, TARDISWeepingAngelsCompat tARDISWeepingAngelsCompat, MysteriousHalloweenCompat mysteriousHalloweenCompat) {
		this.mobPlugin = mobPlugin;
		this.mobtype = mobtype;
		this.customMobsCompat = customMobsCompat;
		this.messages = messages;
		this.mythicMobsCompat = mythicMobsCompat;
		this.citizensCompat = citizensCompat;
		this.tARDISWeepingAngelsCompat = tARDISWeepingAngelsCompat;
		this.mysteriousHalloweenCompat = mysteriousHalloweenCompat;
	}

	/**
	 * @return the mob_id
	 */
	public Integer getMob_id() {
		return mob_id;
	}

	/**
	 * @return the plugin_id
	 */
	public MobPlugin getMobPlugin() {
		return mobPlugin;
	}

	/**
	 * @return the mobtype
	 */
	public String getMobtype() {
		return mobtype;
	}

	/**
	 * @param mobtype
	 *            the mobtype to set
	 */
	public void setMobtype(String mobtype) {
		this.mobtype = mobtype;
	}

	@Override
	public int hashCode() {
		return mob_id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ExtendedMob))
			return false;

		ExtendedMob other = (ExtendedMob) obj;

		return mob_id.equals(other.mob_id);
	}

	@Override
	public String toString() {
		return String.format("MobStore: {mob_id: %s, plugin_id: %s, mobtype: %s}", this.mob_id, mobPlugin.name(),
				mobtype);
	}

	public String getMobName() {
		switch (mobPlugin) {
		case Minecraft:
			return mobtype;
		case MythicMobs:
			String name = mythicMobsCompat.getMobRewardData().get(mobtype).getMobName();
			if (name == null || name.equals(""))
				name = mythicMobsCompat.getMobRewardData().get(mobtype).getMobType();
			return name;
		case Citizens:
			NPC npc = citizensCompat.getCitizensPlugin().getNPCRegistry().getById(Integer.valueOf(mobtype));
			if (npc != null)
				return npc.getName();
			else
				return "";
		case TARDISWeepingAngels:
			return tARDISWeepingAngelsCompat.getMobRewardData().get(mobtype).getMobName();
		case CustomMobs:
			return customMobsCompat.getMobRewardData().get(mobtype).getMobName();
		case MysteriousHalloween:
			return mysteriousHalloweenCompat.getMobRewardData().get(mobtype).getMobName();
		case SmartGiants:
			return "SmartGiant";
		case InfernalMobs:
			return "Infernal " + mobtype;
		default:
			ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
			console.sendMessage(
					ChatColor.RED + "[MobHunting] Missing pluginType '" + mobPlugin.name() + "' in ExtendeMob.");
		}
		return null;
	}

	public String getFriendlyName() {
		if (mobPlugin == MobPlugin.Minecraft)
			return messages.getString("mobs." + mobtype + ".name");
		else
			return messages.getString("mobs." + mobPlugin.name() + "_" + mobtype + ".name");
	}

	// todo why doing that shit splitt it! Interface with it!
	public int getProgressAchievementLevel1() {
		switch (mobPlugin) {
		case Minecraft:
			return MinecraftMob.getMinecraftMobType(mobtype).getProgressAchievementLevel1();
		case MythicMobs:
			return mythicMobsCompat.getProgressAchievementLevel1(mobtype);
		case Citizens:
			return citizensCompat.getProgressAchievementLevel1(mobtype);
		case MysteriousHalloween:
			return mysteriousHalloweenCompat.getProgressAchievementLevel1(mobtype);
		case TARDISWeepingAngels:
			return tARDISWeepingAngelsCompat.getProgressAchievementLevel1(mobtype);
		case CustomMobs:
			return customMobsCompat.getProgressAchievementLevel1(mobtype);
		case SmartGiants:
			return SmartGiantsCompat.getProgressAchievementLevel1(mobtype);
		case InfernalMobs:
			return InfernalMobsCompat.getProgressAchievementLevel1(mobtype);
		default:
			ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
			console.sendMessage(
					ChatColor.RED + "[MobHunting] Missing pluginType '" + mobPlugin.name() + "' in ExtendeMob.");
		}
		return 0;
	}

	public ItemStack getCustomHead(String name, int amount, int money) {
		switch (mobPlugin) {
		case Minecraft:
			return MinecraftMob.getMinecraftMobType(name).getCustomHead(name, amount, money);
		default:
			return new ItemStack(Material.IRON_INGOT, amount);
		}
	}

	public boolean matches(Entity entity) {
		ExtendedMob mob = MobHunting.getInstance().getExtendedMobManager().getExtendedMobFromEntity(entity);
		return mobtype.equalsIgnoreCase(mob.mobtype);
	}

}
