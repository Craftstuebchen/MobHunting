package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class ItsMagic extends Achievement implements Listener {

	public ItsMagic(ConfigManager configManager, AchievementManager achievementManager, ExtendedMobManager extendedMobManager) {
		super(configManager, achievementManager, extendedMobManager, messages);
	}

	@Override
	public String getName() {
		return Messages.getString("achievements.itsmagic.name");
	}

	@Override
	public String getID() {
		return "itsmagic";
	}

	@Override
	public String getDescription() {
		return Messages.getString("achievements.itsmagic.description");
	}

	@Override
	public double getPrize() {
		return configManager.specialItsMagic;
	}

	@EventHandler
	public void onKill(MobHuntKillEvent event) {
		if (event.getDamageInfo().getWeapon().getType() == Material.POTION
				&& configManager.getBaseKillPrize(event.getKilledEntity()) > 0)
			achievementManager.awardAchievement(this, event.getPlayer(),
					extendedMobManager.getExtendedMobFromEntity(event.getKilledEntity()));
	}

	@Override
	public String getPrizeCmd() {
		return configManager.specialItsMagicCmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return configManager.specialItsMagicCmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return new ItemStack(Material.POTION);
	}
}
