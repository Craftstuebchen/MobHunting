package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class ByTheBook extends Achievement implements Listener {

    public ByTheBook(ConfigManager configManager, AchievementManager achievementManager, ExtendedMobManager extendedMobManager, Messages messages) {
        super(configManager, achievementManager, extendedMobManager, messages);
    }

    @Override
    public String getName() {
        return messages.getString("achievements.bythebook.name");
    }

    @Override
    public String getID() {
        return "bythebook"; //$NON-NLS-1$
    }

    @Override
    public String getDescription() {
        return messages.getString("achievements.bythebook.description");
    }

    @Override
    public double getPrize() {
        return configManager.specialByTheBook;
    }

    @EventHandler
    public void onKill(MobHuntKillEvent event) {
        if ((event.getDamageInfo().getWeapon().getType() == Material.BOOK
                || event.getDamageInfo().getWeapon().getType() == Material.WRITTEN_BOOK
                || event.getDamageInfo().getWeapon().getType() == Material.BOOK_AND_QUILL)
                && (configManager.getBaseKillPrize(event.getKilledEntity()) > 0))
            achievementManager.awardAchievement(this, event.getPlayer(),
                    extendedMobManager.getExtendedMobFromEntity(event.getKilledEntity()));
    }

    @Override
    public String getPrizeCmd() {
        return configManager.specialByTheBookCmd;
    }

    @Override
    public String getPrizeCmdDescription() {
        return configManager.specialByTheBookCmdDesc;
    }

    @Override
    public ItemStack getSymbol() {
        return new ItemStack(Material.BOOK);
    }
}
