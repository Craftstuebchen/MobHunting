package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.mobs.ExtendedMob;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import one.lindegaard.MobHunting.mobs.MobPlugin;
import org.bukkit.inventory.ItemStack;

public class SecondHuntAchievement extends ProgressAchievement {


	public SecondHuntAchievement(ConfigManager configManager, AchievementManager achievementManager, ExtendedMobManager extendedMobManager, ExtendedMob extendedMob) {
		super(configManager, achievementManager, extendedMobManager, extendedMob);
	}

	@Override
	public String getName() {
		return Messages.getString("achievements.hunter.2.name", "mob", extendedMob.getFriendlyName());
	}

	@Override
	public String getID() {
		if (extendedMob.getMobPlugin() == MobPlugin.Minecraft)
			return "hunting-level2-" + extendedMob.getMobName().toLowerCase();
		else
			return extendedMob.getMobPlugin().name().toLowerCase() + "-hunting-level2-" + extendedMob.getMobtype().toLowerCase();

	}

	@Override
	public String getDescription() {
		return Messages.getString("achievements.hunter.2.description", "count", getNextLevel(), "mob",
				extendedMob.getFriendlyName());
	}

	@Override
	public double getPrize() {
		return configManager.specialHunter2;
	}

	@Override
	public int getNextLevel() {
		return (int) Math.round(extendedMob.getProgressAchievementLevel1() * 2.5);
	}

	@Override
	public String inheritFrom() {
		if (extendedMob.getMobPlugin() == MobPlugin.Minecraft)
			return "hunting-level1-" + extendedMob.getMobtype().toLowerCase();
		else
			return extendedMob.getMobPlugin().name().toLowerCase() + "-hunting-level1-" + extendedMob.getMobtype().toLowerCase();
	}

	@Override
	public String nextLevelId() {
		if (extendedMob.getMobPlugin() == MobPlugin.Minecraft)
			return "hunting-level3-" + extendedMob.getMobtype().toLowerCase();
		else
			return extendedMob.getMobPlugin().name().toLowerCase() + "-hunting-level3-" + extendedMob.getMobtype().toLowerCase();
	}

	@Override
	public String getPrizeCmd() {
		return configManager.specialHunter2Cmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return configManager.specialHunter2CmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return getExtendedMob().getCustomHead(extendedMob.getMobName(), 2, 0);
	}

	@Override
	public ExtendedMob getExtendedMob() {
		return extendedMob;
	}
}
