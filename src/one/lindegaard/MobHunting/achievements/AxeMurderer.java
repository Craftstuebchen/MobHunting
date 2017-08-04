package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import one.lindegaard.MobHunting.util.Misc;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class AxeMurderer extends Achievement implements Listener {


	public AxeMurderer(ConfigManager configManager,Messages messages, AchievementManager achievementManager, ExtendedMobManager extendedMobManager) {
		super(configManager,achievementManager,extendedMobManager, messages);
	}

	@Override
	public String getName() {
		return messages.getString("achievements.axemurderer.name");
	}

	@Override
	public String getID() {
		return "axemurderer";
	}

	@Override
	public String getDescription() {
		return messages.getString("achievements.axemurderer.description");
	}

	@Override
	public double getPrize() {
		return configManager.specialAxeMurderer;
	}

	@EventHandler
	public void onKill(MobHuntKillEvent event) {
		if (Misc.isAxe(event.getDamageInfo().getWeapon())
				&& configManager.getBaseKillPrize(event.getKilledEntity()) > 0)
			achievementManager.awardAchievement(this, event.getPlayer(),
					extendedMobManager.getExtendedMobFromEntity(event.getKilledEntity()));
	}

	@Override
	public String getPrizeCmd() {
		return configManager.specialAxeMurdererCmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return configManager.specialAxeMurdererCmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return new ItemStack(Material.WOOD_AXE);
	}

}
