package one.lindegaard.MobHunting.modifier;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.DamageInformation;
import one.lindegaard.MobHunting.HuntData;
import one.lindegaard.MobHunting.Messages;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class SniperBonus extends IModifier {

	public SniperBonus(ConfigManager configManager) {
		super(configManager);
	}

	@Override
	public String getName() {
		return ChatColor.GRAY + Messages.getString("bonus.sniper.name");
	}

	@Override
	public double getMultiplier(Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo,
			EntityDamageByEntityEvent lastDamageCause) {
		return 1 + ((configManager.bonusFarShot - 1) / 2);
	}

	@Override
	public boolean doesApply(Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo,
			EntityDamageByEntityEvent lastDamageCause) {
		if (extraInfo.getWeapon().getType() == Material.BOW && !extraInfo.isMeleWeapenUsed()) {
			double dist = extraInfo.getAttackerPosition().distance(deadEntity.getLocation());
			if (dist >= 20 && dist < 50)
				return true;
		}

		return false;
	}

}
