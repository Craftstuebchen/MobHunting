package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class JustInTime extends Achievement implements Listener {

	public JustInTime(ConfigManager configManager, AchievementManager achievementManager, ExtendedMobManager extendedMobManager) {
		super(configManager, achievementManager, extendedMobManager, messages);
	}

	@Override
	public String getName() {
		return Messages.getString("achievements.justintime.name");
	}

	@Override
	public String getID() {
		return "justintime";
	}

	@Override
	public String getDescription() {
		return Messages.getString("achievements.justintime.description");
	}

	@Override
	public double getPrize() {
		return configManager.specialJustInTime;
	}

	@EventHandler
	public void onKill(MobHuntKillEvent event) {
		// getTime() return world time in ticks. 0 ticks = 6:00 500=6:30
		// Zombies begin burning about 5:30 = 23500
		// player get a reward if he kills between 5:30 and 6:00.
		if (event.getKilledEntity().getWorld().getEnvironment().equals(Environment.NORMAL)
				&& configManager.getBaseKillPrize(event.getKilledEntity()) > 0
				&& (event.getKilledEntity().getWorld().getTime() >= 23500
						&& event.getKilledEntity().getWorld().getTime() <= 24000)
				&& event.getKilledEntity().getFireTicks() > 0)
			achievementManager.awardAchievement(this, event.getPlayer(),
					extendedMobManager.getExtendedMobFromEntity(event.getKilledEntity()));
	}

	@Override
	public String getPrizeCmd() {
		return configManager.specialJustInTimeCmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return configManager.specialJustInTimeCmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return new ItemStack(Material.WATCH);
	}
}
