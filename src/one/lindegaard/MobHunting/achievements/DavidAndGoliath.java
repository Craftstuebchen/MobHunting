package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.compatibility.SmartGiantsCompat;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class DavidAndGoliath extends Achievement implements Listener {

	public DavidAndGoliath(ConfigManager configManager, AchievementManager achievementManager, ExtendedMobManager extendedMobManager) {
		super(configManager, achievementManager, extendedMobManager, messages);
	}

	@Override
	public String getName() {
		return Messages.getString("achievements.davidandgoliath.name");
	}

	@Override
	public String getID() {
		return "davidandgoliath";
	}

	@Override
	public String getDescription() {
		return Messages.getString("achievements.davidandgoliath.description");
	}

	@Override
	public double getPrize() {
		return configManager.davidAndGoliat;
	}

	@EventHandler
	public void onKill(MobHuntKillEvent event) {
		if (SmartGiantsCompat.isSmartGiants(event.getKilledEntity())
				&& event.getDamageInfo().getWeapon().getType() == Material.STONE_BUTTON
				&& !(configManager.getBaseKillPrize(event.getKilledEntity()) == 0
						&& configManager.getKillConsoleCmd(event.getKilledEntity()).isEmpty()))
			achievementManager.awardAchievement(this, event.getPlayer(),
					extendedMobManager.getExtendedMobFromEntity(event.getKilledEntity()));
	}

	@Override
	public String getPrizeCmd() {
		return configManager.davidAndGoliatCmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return configManager.davidAndGoliatCmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return new ItemStack(Material.SKULL_ITEM, 1, (short) 2);
	}

}
