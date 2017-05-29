package one.lindegaard.MobHunting.achievements;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;

public class TheHuntBegins implements Achievement, Listener {
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
		return MobHunting.getConfigManager().specialHuntBegins;
	}

	@EventHandler
	private void onKill(MobHuntKillEvent event) {
		if (MobHunting.getConfigManager().getBaseKillPrize(event.getKilledEntity()) > 0)
			MobHunting.getAchievementManager().awardAchievement(this, event.getPlayer(),
					MobHunting.getExtendedMobManager().getExtendedMobFromEntity(event.getKilledEntity()));
	}

	@Override
	public String getPrizeCmd() {
		return MobHunting.getConfigManager().specialHuntBeginsCmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return MobHunting.getConfigManager().specialHuntBeginsCmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return new ItemStack(Material.COAL);
	}
}
