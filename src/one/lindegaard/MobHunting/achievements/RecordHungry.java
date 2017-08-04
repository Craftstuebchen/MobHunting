package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHuntingManager;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class RecordHungry extends Achievement implements Listener {

	private MobHuntingManager mobHuntingManager;

	public RecordHungry(ConfigManager configManager, AchievementManager achievementManager, ExtendedMobManager extendedMobManager, MobHuntingManager mobHuntingManager) {
		super(configManager, achievementManager, extendedMobManager, messages);
		this.mobHuntingManager = mobHuntingManager;
	}

	@Override
	public String getName() {
		return Messages.getString("achievements.recordhungry.name");
	}

	@Override
	public String getID() {
		return "recordhungry";
	}

	@Override
	public String getDescription() {
		return Messages.getString("achievements.recordhungry.description");
	}

	@Override
	public double getPrize() {
		return configManager.specialRecordHungry;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDeath(MobHuntKillEvent event) {
		if (!(event.getKilledEntity() instanceof Creeper)
				|| !mobHuntingManager.isHuntEnabledInWorld(event.getKilledEntity().getWorld())
				|| (configManager.getBaseKillPrize(event.getKilledEntity()) <= 0))
			return;

		Creeper killed = (Creeper) event.getKilledEntity();

		if (!(killed.getLastDamageCause() instanceof EntityDamageByEntityEvent))
			return;

		EntityDamageByEntityEvent damage = (EntityDamageByEntityEvent) killed.getLastDamageCause();

		if (damage.getDamager() instanceof Arrow && ((Arrow) damage.getDamager()).getShooter() instanceof Skeleton) {
			Skeleton skele = (Skeleton) ((Arrow) damage.getDamager()).getShooter();

			if (killed.getTarget() instanceof Player) {
				Player target = (Player) killed.getTarget();

				if (skele.getTarget() == target && target.getGameMode() != GameMode.CREATIVE
						&& mobHuntingManager.isHuntEnabled(target))
					achievementManager.awardAchievement(this, target,
							extendedMobManager.getExtendedMobFromEntity(event.getKilledEntity()));
			}
		}
	}

	@Override
	public String getPrizeCmd() {
		return configManager.specialRecordHungryCmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return configManager.specialRecordHungryCmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return new ItemStack(Material.BREAD);
	}
}
