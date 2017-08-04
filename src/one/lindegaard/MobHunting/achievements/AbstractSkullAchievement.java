package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public abstract class AbstractSkullAchievement extends Achievement{

    public AbstractSkullAchievement(ConfigManager configManager, AchievementManager achievementManager, ExtendedMobManager extendedMobManager, Messages messages) {
        super(configManager, achievementManager, extendedMobManager, messages);
    }

    @Override
    public ItemStack getSymbol() {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 4);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwner("MHF_Creeper");
        skull.setItemMeta(skullMeta);
        return skull;
    }
}
