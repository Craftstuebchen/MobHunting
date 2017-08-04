package one.lindegaard.MobHunting.modifier;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.DamageInformation;
import one.lindegaard.MobHunting.HuntData;
import one.lindegaard.MobHunting.Messages;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ReturnToSenderBonus extends IModifier {

	public ReturnToSenderBonus(ConfigManager configManager,Messages messages) {
		super(configManager,messages);
	}

	@Override
	public String getName() {
		return ChatColor.GOLD + messages.getString("bonus.returntosender.name");
	}

	@Override
	public double getMultiplier(Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo,
			EntityDamageByEntityEvent lastDamageCause) {
		return configManager.bonusReturnToSender;
	}

	@Override
	public boolean doesApply(Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo,
			EntityDamageByEntityEvent lastDamageCause) {
		if (!(deadEntity instanceof Ghast))
			return false;
		if (!(deadEntity.getLastDamageCause() instanceof EntityDamageByEntityEvent))
			return false;

		return (((EntityDamageByEntityEvent) deadEntity.getLastDamageCause()).getDamager() instanceof LargeFireball);
	}

}
