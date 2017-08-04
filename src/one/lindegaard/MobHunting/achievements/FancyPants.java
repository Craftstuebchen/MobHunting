package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class FancyPants extends Achievement implements Listener {




    public FancyPants(ConfigManager configManager, AchievementManager achievementManager, ExtendedMobManager extendedMobManager,Messages messages) {
        super(configManager, achievementManager, extendedMobManager, messages);
    }

    @Override
	public String getName() {
		return messages.getString("achievements.fancypants.name");
	}

	@Override
	public String getID() {
		return "fancypants";
	}

	@Override
	public String getDescription() {
		return messages.getString("achievements.fancypants.description");
	}

	@Override
	public double getPrize() {
		return configManager.specialFancyPants;
	}

	@EventHandler
	public void onKill(MobHuntKillEvent event) {
		if (event.getDamageInfo().getWeapon().getType() == Material.DIAMOND_SWORD
				&& !event.getDamageInfo().getWeapon().getEnchantments().isEmpty()
				&& event.getPlayer().getInventory().getHelmet() != null
				&& event.getPlayer().getInventory().getHelmet().getType() == Material.DIAMOND_HELMET
				&& !event.getPlayer().getInventory().getHelmet().getEnchantments().isEmpty()
				&& event.getPlayer().getInventory().getChestplate() != null
				&& event.getPlayer().getInventory().getChestplate().getType() == Material.DIAMOND_CHESTPLATE
				&& !event.getPlayer().getInventory().getChestplate().getEnchantments().isEmpty()
				&& event.getPlayer().getInventory().getLeggings() != null
				&& event.getPlayer().getInventory().getLeggings().getType() == Material.DIAMOND_LEGGINGS
				&& !event.getPlayer().getInventory().getLeggings().getEnchantments().isEmpty()
				&& event.getPlayer().getInventory().getBoots() != null
				&& event.getPlayer().getInventory().getBoots().getType() == Material.DIAMOND_BOOTS
				&& !event.getPlayer().getInventory().getBoots().getEnchantments().isEmpty()
				&& configManager.getBaseKillPrize(event.getKilledEntity()) > 0)
			achievementManager.awardAchievement(this, event.getPlayer(),
					extendedMobManager.getExtendedMobFromEntity(event.getKilledEntity()));
	}

	@Override
	public String getPrizeCmd() {
		return configManager.specialFancyPantsCmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return configManager.specialFancyPantsCmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return new ItemStack(Material.DIAMOND_LEGGINGS);
	}
}
