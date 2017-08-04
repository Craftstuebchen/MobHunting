package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.events.MobHuntFishingEvent;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;
import one.lindegaard.MobHunting.mobs.ExtendedMob;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import one.lindegaard.MobHunting.mobs.MobPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class EighthHuntAchievement extends ProgressAchievement implements Listener {


	public EighthHuntAchievement(ConfigManager configManager, AchievementManager achievementManager, ExtendedMobManager extendedMobManager, ExtendedMob extendedMob) {
		super(configManager, achievementManager, extendedMobManager, extendedMob);
	}

	@Override
	public String getName() {
		return Messages.getString("achievements.hunter.8.name", "mob", extendedMob.getFriendlyName());
	}

	@Override
	public String getID() {
		if (extendedMob.getMobPlugin() == MobPlugin.Minecraft)
			return "hunting-level8-" + extendedMob.getMobName().toLowerCase();
		else
			return extendedMob.getMobPlugin().name().toLowerCase() + "-hunting-level8-" + extendedMob.getMobtype().toLowerCase();

	}

	@Override
	public String getDescription() {
		return Messages.getString("achievements.hunter.8.description", "count", getNextLevel(), "mob",
				extendedMob.getFriendlyName());
	}

	@Override
	public double getPrize() {
		return configManager.specialHunter8;
	}

	@Override
	public int getNextLevel() {
		return extendedMob.getProgressAchievementLevel1() * 500;
	}

	@Override
	public String inheritFrom() {
		if (extendedMob.getMobPlugin() == MobPlugin.Minecraft)
			return "hunting-level7-" + extendedMob.getMobtype().toLowerCase();
		else
			return extendedMob.getMobPlugin().name().toLowerCase() + "-hunting-level7-" + extendedMob.getMobtype().toLowerCase();
	}

	@Override
	public String nextLevelId() {
		return null;
	}

	@Override
	public String getPrizeCmd() {
		return configManager.specialHunter8Cmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return configManager.specialHunter8CmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return extendedMob.getCustomHead(extendedMob.getMobName(), 7, 0);
	}

	@Override
	public ExtendedMob getExtendedMob() {
		return extendedMob;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onKillCompleted(MobHuntKillEvent event) {
		if (extendedMob.matches(event.getKilledEntity()))
			achievementManager.awardAchievementProgress(this, event.getPlayer(),
					extendedMobManager.getExtendedMobFromEntity(event.getKilledEntity()), 1);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onFishingCompleted(MobHuntFishingEvent event) {
		if (extendedMob.matches(event.getFish())) {
			achievementManager.awardAchievementProgress(this, event.getPlayer(),
					extendedMobManager.getExtendedMobFromEntity(event.getFish()), 1);
		}
	}
}
