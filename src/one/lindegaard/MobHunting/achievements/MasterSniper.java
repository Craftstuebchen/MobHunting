package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class MasterSniper extends Achievement implements Listener {

	public MasterSniper(ConfigManager configManager, AchievementManager achievementManager, ExtendedMobManager extendedMobManager) {
		super(configManager, achievementManager, extendedMobManager, messages);
	}

	@Override
	public String getName() {
		return Messages.getString("achievements.master-sniper.name");
	}

	@Override
	public String getID() {
		return "master-sniper";
	}

	@Override
	public String getDescription() {
		return Messages.getString("achievements.master-sniper.description");
	}

	@Override
	public double getPrize() {
		return configManager.specialMasterSniper;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onKillCompleted(MobHuntKillEvent event) {
		if (event.getPlayer().isInsideVehicle() && event.getDamageInfo().getWeapon().getType() == Material.BOW
				&& !event.getDamageInfo().isMeleWeapenUsed()
				&& event.getPlayer().getVehicle().getVelocity().length() > 0.2
				&& configManager.getBaseKillPrize(event.getKilledEntity()) > 0) {
			double dist = event.getDamageInfo().getAttackerPosition().distance(event.getKilledEntity().getLocation());
			if (dist >= 40) {
				achievementManager.awardAchievement(this, event.getPlayer(),
						extendedMobManager.getExtendedMobFromEntity(event.getKilledEntity()));
			}
		}
	}

	@Override
	public String getPrizeCmd() {
		return configManager.specialMasterSniperCmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return configManager.specialMasterSniperCmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return new ItemStack(Material.BOW);
	}
}
