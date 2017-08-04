package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class TheHuntBegins extends Achievement implements Listener {
	public TheHuntBegins(ConfigManager configManager, AchievementManager achievementManager, ExtendedMobManager extendedMobManager) {
		super(configManager, achievementManager, extendedMobManager, messages);
	}

	@Override
	public String getName() {
		return Messages.getString("achievements.huntbegins.name");
	}

	@Override
	public String getID() {
		return "huntbegins";
	}

	@Override
	public String getDescription() {
		return Messages.getString("achievements.huntbegins.description");
	}

	@Override
	public double getPrize() {
		return configManager.specialHuntBegins;
	}

	@EventHandler
	public void onKill(MobHuntKillEvent event) {
		Entity killedEntity = event.getKilledEntity();
		if (configManager.getBaseKillPrize(killedEntity) != 0
				|| !configManager.getKillConsoleCmd(killedEntity).isEmpty())
			achievementManager.awardAchievement(this, event.getPlayer(),
					extendedMobManager.getExtendedMobFromEntity(killedEntity));
	}

	@Override
	public String getPrizeCmd() {
		return configManager.specialHuntBeginsCmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return configManager.specialHuntBeginsCmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return new ItemStack(Material.COAL);
	}
}
