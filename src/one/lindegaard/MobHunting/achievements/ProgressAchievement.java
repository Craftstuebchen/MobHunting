package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.mobs.ExtendedMob;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;

public abstract class ProgressAchievement extends Achievement
{

	protected ExtendedMob extendedMob;

	public ProgressAchievement(ConfigManager configManager, AchievementManager achievementManager, ExtendedMobManager extendedMobManager, Messages messages,ExtendedMob extendedMob) {
		super(configManager, achievementManager, extendedMobManager, messages);
        this.extendedMob = extendedMob;
	}

	public  abstract int getNextLevel();

	public  abstract String inheritFrom();

	public  abstract String nextLevelId();

	public  abstract ExtendedMob getExtendedMob();
}
