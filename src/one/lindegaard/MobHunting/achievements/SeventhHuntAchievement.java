package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.mobs.ExtendedMob;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import one.lindegaard.MobHunting.mobs.MobPlugin;
import org.bukkit.inventory.ItemStack;

public class SeventhHuntAchievement extends ProgressAchievement {


	public SeventhHuntAchievement(ConfigManager configManager, AchievementManager achievementManager, ExtendedMobManager extendedMobManager, ExtendedMob extendedMob) {
		super(configManager, achievementManager, extendedMobManager, extendedMob);
	}

	@Override
	public String getName() {
		return Messages.getString("achievements.hunter.7.name", "mob", extendedMob.getFriendlyName());
	}

	@Override
	public String getID() {
		if (extendedMob.getMobPlugin() == MobPlugin.Minecraft)
			return "hunting-level7-" + extendedMob.getMobName().toLowerCase();
		else
			return extendedMob.getMobPlugin().name().toLowerCase() + "-hunting-level7-" + extendedMob.getMobtype().toLowerCase();

	}

	@Override
	public String getDescription() {
		return Messages.getString("achievements.hunter.7.description", "count", getNextLevel(), "mob",
				extendedMob.getFriendlyName());
	}

	@Override
	public double getPrize() {
		return configManager.specialHunter7;
	}

	@Override
	public int getNextLevel() {
		return extendedMob.getProgressAchievementLevel1() * 100;
	}

	@Override
	public String inheritFrom() {
		if (extendedMob.getMobPlugin() == MobPlugin.Minecraft)
			return "hunting-level6-" + extendedMob.getMobtype().toLowerCase();
		else
			return extendedMob.getMobPlugin().name().toLowerCase() + "-hunting-level6-" + extendedMob.getMobtype().toLowerCase();
	}

	@Override
	public String nextLevelId() {
		if (extendedMob.getMobPlugin() == MobPlugin.Minecraft)
			return "hunting-level8-" + extendedMob.getMobtype().toLowerCase();
		else
			return extendedMob.getMobPlugin().name().toLowerCase() + "-hunting-level8-" + extendedMob.getMobtype().toLowerCase();
	}

	@Override
	public String getPrizeCmd() {
		return configManager.specialHunter7Cmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return configManager.specialHunter7CmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return extendedMob.getCustomHead(extendedMob.getMobName(), 7, 0);
	}

	@Override
	public ExtendedMob getExtendedMob() {
		return extendedMob;
	}

}
