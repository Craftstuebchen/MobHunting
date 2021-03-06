package one.lindegaard.MobHunting.modifier;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.DamageInformation;
import one.lindegaard.MobHunting.HuntData;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.compatibility.MobStackerCompat;
import one.lindegaard.MobHunting.compatibility.StackMobCompat;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class StackedMobBonus extends IModifier {

	public StackedMobBonus(ConfigManager configManager) {
		super(configManager);
	}

	@Override
	public String getName() {
		return ChatColor.AQUA + Messages.getString("bonus.mobstacker.name");
	}

	@Override
	public double getMultiplier(Entity entity, Player killer, HuntData data, DamageInformation extraInfo,
			EntityDamageByEntityEvent lastDamageCause) {
		if (MobStackerCompat.isSupported() && MobStackerCompat.killHoleStackOnDeath(entity)
				&& MobStackerCompat.multiplyLoot()) {
			Messages.debug("StackedMobBonus: Pay reward for no %s mob", MobStackerCompat.getStackSize(entity));
			return MobStackerCompat.getStackSize(entity);
		} else if (StackMobCompat.isSupported() && StackMobCompat.killHoleStackOnDeath(entity)) {
			Messages.debug("StackedMobBonus: Pay reward for no %s mob", StackMobCompat.getStackSize(entity));
			return StackMobCompat.getStackSize(entity);
		} else {
			Messages.debug("StackedMobBonus: Pay reward for one mob");
			return 1;
		}
	}

	@Override
	public boolean doesApply(Entity entity, Player killer, HuntData data, DamageInformation extraInfo,
			EntityDamageByEntityEvent lastDamageCause) {
		return MobStackerCompat.isStackedMob(entity) || StackMobCompat.isStackedMob(entity);
	}
}
