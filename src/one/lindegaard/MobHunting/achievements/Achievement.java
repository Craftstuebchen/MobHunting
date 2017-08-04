package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import org.bukkit.inventory.ItemStack;

public abstract class Achievement {

    protected ConfigManager configManager;
    protected AchievementManager achievementManager;
    protected ExtendedMobManager extendedMobManager;
    protected Messages messages;

    public Achievement(ConfigManager configManager, AchievementManager achievementManager, ExtendedMobManager extendedMobManager, Messages messages){
        this.configManager=configManager;
        this.achievementManager = achievementManager;
        this.extendedMobManager = extendedMobManager;
        this.messages = messages;
    }

    public abstract String getName();

    public abstract String getID();

    public abstract String getDescription();

    public abstract double getPrize();

    public abstract String getPrizeCmd();

    public abstract String getPrizeCmdDescription();

    public abstract ItemStack getSymbol();
}
