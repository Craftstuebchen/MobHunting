package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.mobs.ExtendedMob;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import one.lindegaard.MobHunting.mobs.MobPlugin;
import org.bukkit.inventory.ItemStack;

public class BasicHuntAchievement extends ProgressAchievement {

	public BasicHuntAchievement(ConfigManager configManager, AchievementManager achievementManager, ExtendedMobManager extendedMobManager, ExtendedMob extendedMob) {
		super(configManager, achievementManager, extendedMobManager, extendedMob);
	}

	@Override
	public String getName() {
		return Messages.getString("achievements.hunter.1.name", "mob", extendedMob.getFriendlyName());
	}

	@Override
	public String getID() {
		if (extendedMob.getMobPlugin() == MobPlugin.Minecraft)
			return "hunting-level1-" + extendedMob.getMobName().toLowerCase();
		else
			return extendedMob.getMobPlugin().name().toLowerCase() + "-hunting-level1-" + extendedMob.getMobtype().toLowerCase();

	}

	@Override
	public String getDescription() {
		return Messages.getString("achievements.hunter.1.description", "count", getNextLevel(), "mob",
				extendedMob.getFriendlyName());
	}

	@Override
	public double getPrize() {
		return configManager.specialHunter1;
	}

	@Override
	public int getNextLevel() {
		return extendedMob.getProgressAchievementLevel1();
	}

	@Override
	public String inheritFrom() {
		return null;
	}

	@Override
	public String nextLevelId() {
		if (extendedMob.getMobPlugin() == MobPlugin.Minecraft)
			return "hunting-level2-" + extendedMob.getMobtype().toLowerCase();
		else
			return extendedMob.getMobPlugin() + "-hunting-level2-" + extendedMob.getMobtype().toLowerCase();
	}

	@Override
	public String getPrizeCmd() {
		return configManager.specialHunter1Cmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return configManager.specialHunter1CmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return extendedMob.getCustomHead(extendedMob.getMobName(), 1, 0);
	}

	@Override
	public ExtendedMob getExtendedMob() {
		return extendedMob;
	}
}
