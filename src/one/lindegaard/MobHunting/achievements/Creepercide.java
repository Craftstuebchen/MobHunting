package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.DamageInformation;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHuntingManager;
import one.lindegaard.MobHunting.compatibility.MobArenaCompat;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class Creepercide extends AbstractSkullAchievement implements Listener {

	private final MobHuntingManager mobHuntingManager;

	public Creepercide(ConfigManager configManager, AchievementManager achievementManager, ExtendedMobManager extendedMobManager, Messages messages, MobHuntingManager mobHuntingManager) {
		super(configManager, achievementManager, extendedMobManager, messages);
		this.mobHuntingManager = mobHuntingManager;
	}

	@Override
	public String getName() {
		return messages.getString("achievements.creepercide.name");
	}

	@Override
	public String getID() {
		return "creepercide";
	}

	@Override
	public String getDescription() {
		return messages.getString("achievements.creepercide.description");
	}

	@Override
	public double getPrize() {
		return configManager.specialCreepercide;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onKill(MobHuntKillEvent event) {
		if (!(event.getKilledEntity() instanceof Creeper)
				|| !mobHuntingManager.isHuntEnabledInWorld(event.getKilledEntity().getWorld()))
			return;

		if (configManager.getBaseKillPrize(event.getKilledEntity()) <= 0)
			return;

		Creeper killed = (Creeper) event.getKilledEntity();

		if (!(killed.getLastDamageCause() instanceof EntityDamageByEntityEvent))
			return;

		EntityDamageByEntityEvent damage = (EntityDamageByEntityEvent) killed.getLastDamageCause();

		if (damage.getDamager() instanceof Creeper) {
			Player initiator = null;

			if (((Creeper) event.getKilledEntity()).getTarget() instanceof Player)
				initiator = (Player) ((Creeper) event.getKilledEntity()).getTarget();
			else {
				DamageInformation a, b;
				a = mobHuntingManager.getDamageInformation(killed);
				b = mobHuntingManager.getDamageInformation((Creeper) damage.getDamager());

				if (a != null)
					initiator = a.getAttacker();

				if (b != null && initiator == null)
					initiator = b.getAttacker();
			}

			if (initiator != null && mobHuntingManager.isHuntEnabled(initiator)) {
				// Check if player (initiator) is playing MobArena.
				if (MobArenaCompat.isPlayingMobArena((Player) initiator)
						&& !configManager.mobarenaGetRewards) {
					messages.debug("AchiveBlocked: CreeperCide was achieved while %s was playing MobArena.",
							initiator.getName());
					messages.learn(initiator, messages.getString("mobhunting.learn.mobarena"));
				} else
					achievementManager.awardAchievement("creepercide", initiator,
							extendedMobManager.getExtendedMobFromEntity(event.getKilledEntity()));
			}
		}
	}

	@Override
	public String getPrizeCmd() {
		return configManager.specialCreepercideCmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return configManager.specialCreepercideCmdDesc;
	}
}
