package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.mobs.ExtendedMob;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import one.lindegaard.MobHunting.mobs.MobPlugin;
import org.bukkit.inventory.ItemStack;

public class FifthHuntAchievement extends ProgressAchievement {


    public FifthHuntAchievement(ConfigManager configManager, AchievementManager achievementManager, ExtendedMobManager extendedMobManager, ExtendedMob extendedMob) {
        super(configManager, achievementManager, extendedMobManager, extendedMob);
    }

    @Override
	public String getName() {
		return Messages.getString("achievements.hunter.5.name", "mob", extendedMob.getFriendlyName());
	}

	@Override
	public String getID() {
		if (extendedMob.getMobPlugin() == MobPlugin.Minecraft)
			return "hunting-level5-" + extendedMob.getMobName().toLowerCase();
		else
			return extendedMob.getMobPlugin().name().toLowerCase() + "-hunting-level5-" + extendedMob.getMobtype().toLowerCase();

	}

	@Override
	public String getDescription() {
		return Messages.getString("achievements.hunter.5.description", "count", getNextLevel(), "mob",
                extendedMob.getFriendlyName());
	}

	@Override
	public double getPrize() {
		return configManager.specialHunter5;
	}

	@Override
	public int getNextLevel() {
		return extendedMob.getProgressAchievementLevel1() * 25;
	}

	@Override
	public String inheritFrom() {
		if (extendedMob.getMobPlugin() == MobPlugin.Minecraft)
			return "hunting-level4-" + extendedMob.getMobtype().toLowerCase();
		else
			return extendedMob.getMobPlugin().name().toLowerCase() + "-hunting-level4-" + extendedMob.getMobtype().toLowerCase();
	}

	@Override
	public String nextLevelId() {
		if (extendedMob.getMobPlugin() == MobPlugin.Minecraft)
			return "hunting-level6-" + extendedMob.getMobtype().toLowerCase();
		else
			return extendedMob.getMobPlugin().name().toLowerCase() + "-hunting-level6-" + extendedMob.getMobtype().toLowerCase();
	}

	@Override
	public String getPrizeCmd() {
		return configManager.specialHunter5Cmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return configManager.specialHunter5CmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return extendedMob.getCustomHead(extendedMob.getMobName(), 5, 0);
	}

	@Override
	public ExtendedMob getExtendedMob() {
		return extendedMob;
	}
}
