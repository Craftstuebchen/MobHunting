package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.DamageInformation;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHuntingManager;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class InFighting extends Achievement implements Listener {

	private MobHuntingManager mobHuntingManager;

	public InFighting(ConfigManager configManager, AchievementManager achievementManager, ExtendedMobManager extendedMobManager, MobHuntingManager mobHuntingManager) {
		super(configManager, achievementManager, extendedMobManager, messages);
		this.mobHuntingManager = mobHuntingManager;
	}

	@Override
	public String getName() {
		return Messages.getString("achievements.infighting.name");
	}

	@Override
	public String getID() {
		return "infighting";
	}

	@Override
	public String getDescription() {
		return Messages.getString("achievements.infighting.description");
	}

	@Override
	public double getPrize() {
		return configManager.specialInfighting;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onKill(MobHuntKillEvent event) {
		if (!(event.getKilledEntity() instanceof Skeleton)
				|| !mobHuntingManager.isHuntEnabledInWorld(event.getKilledEntity().getWorld())
				|| configManager.getBaseKillPrize(event.getKilledEntity()) <= 0)
			return;

		Skeleton killed = (Skeleton) event.getKilledEntity();

		if (!(killed.getLastDamageCause() instanceof EntityDamageByEntityEvent))
			return;

		EntityDamageByEntityEvent damage = (EntityDamageByEntityEvent) killed.getLastDamageCause();

		if (damage.getDamager() instanceof Arrow && ((Arrow) damage.getDamager()).getShooter() instanceof Skeleton) {
			Skeleton skele = (Skeleton) ((Arrow) damage.getDamager()).getShooter();

			if (killed.getTarget() == skele && skele.getTarget() == killed) {
				DamageInformation a, b;
				a = mobHuntingManager.getDamageInformation(killed);
				b = mobHuntingManager.getDamageInformation(skele);

				Player initiator = null;
				if (a != null)
					initiator = a.getAttacker();

				if (b != null && initiator == null)
					initiator = b.getAttacker();

				if (initiator != null && mobHuntingManager.isHuntEnabled(initiator))
					achievementManager.awardAchievement(this, initiator,
							extendedMobManager.getExtendedMobFromEntity(event.getKilledEntity()));
			}
		}
	}

	@Override
	public String getPrizeCmd() {
		return configManager.specialInfightingCmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return configManager.specialInfightingCmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return new ItemStack(Material.IRON_SWORD);
	}
}
