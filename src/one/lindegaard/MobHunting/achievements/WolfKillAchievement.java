package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHuntingManager;
import one.lindegaard.MobHunting.compatibility.CustomMobsCompat;
import one.lindegaard.MobHunting.compatibility.MobArenaCompat;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;
import one.lindegaard.MobHunting.mobs.ExtendedMob;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import one.lindegaard.MobHunting.mobs.MinecraftMob;
import one.lindegaard.MobHunting.mobs.MobPlugin;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class WolfKillAchievement extends ProgressAchievement implements Listener {

	private MobHuntingManager mobHuntingManager;
	private CustomMobsCompat customMobsCompat;

	public WolfKillAchievement(ConfigManager configManager, Messages messages, AchievementManager achievementManager, ExtendedMobManager extendedMobManager,  ExtendedMob extendedMob, MobHuntingManager mobHuntingManager, CustomMobsCompat customMobsCompat) {
		super(configManager, achievementManager, extendedMobManager, messages,extendedMob);
		this.mobHuntingManager = mobHuntingManager;
		this.customMobsCompat = customMobsCompat;
	}

	@Override
	public String getName() {
		return messages.getString("achievements.fangmaster.name");
	}

	@Override
	public String getID() {
		return "fangmaster";
	}

	@Override
	public String getDescription() {
		return messages.getString("achievements.fangmaster.description");
	}

	@Override
	public double getPrize() {
		return configManager.specialFangMaster;
	}

	@Override
	public int getNextLevel() {
		return 500;
	}

	@Override
	public String inheritFrom() {
		return null;
	}

	@Override
	public String nextLevelId() {
		return null;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onWolfKillMob(MobHuntKillEvent event) {
		if (!mobHuntingManager.isHuntEnabledInWorld(event.getKilledEntity().getWorld())
				|| !(event.getKilledEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent)
				|| (configManager.getBaseKillPrize(event.getKilledEntity()) <= 0))
			return;

		EntityDamageByEntityEvent dmg = (EntityDamageByEntityEvent) event.getKilledEntity().getLastDamageCause();

		if (!(dmg.getDamager() instanceof Wolf))
			return;

		Wolf killer = (Wolf) dmg.getDamager();

		if (killer.isTamed() && killer.getOwner() instanceof OfflinePlayer) {
			Player owner = ((OfflinePlayer) killer.getOwner()).getPlayer();

			if (owner != null && mobHuntingManager.isHuntEnabled(owner)) {
				if (MobArenaCompat.isPlayingMobArena(owner)
						&& !configManager.mobarenaGetRewards) {
					messages.debug("AchiveBlocked: FangMaster was achieved while %s was playing MobArena.",
							owner.getName());
					messages.learn(owner, messages.getString("mobhunting.learn.mobarena"));
				} else
					achievementManager.awardAchievementProgress(this, owner,
							extendedMobManager.getExtendedMobFromEntity(event.getKilledEntity()), 1);
			}
		}

	}

	@Override
	public String getPrizeCmd() {
		return configManager.specialFangMasterCmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return configManager.specialFangMasterCmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return new ItemStack(Material.STRING);
	}

	@Override
	public ExtendedMob getExtendedMob() {
		return new ExtendedMob(MobPlugin.Minecraft, MinecraftMob.Wolf.name(), customMobsCompat, messages, mythicMobsCompat, citizensCompat, tARDISWeepingAngelsCompat, mysteriousHalloweenCompat);
	}
}
