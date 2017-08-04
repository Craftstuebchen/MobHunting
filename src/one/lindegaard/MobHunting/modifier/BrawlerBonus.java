package one.lindegaard.MobHunting.modifier;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.DamageInformation;
import one.lindegaard.MobHunting.HuntData;
import one.lindegaard.MobHunting.Messages;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class BrawlerBonus extends IModifier {

	public BrawlerBonus(ConfigManager configManager) {
		super(configManager);
	}

	@Override
	public String getName() {
		return ChatColor.LIGHT_PURPLE + Messages.getString("bonus.brawler.name");
	}

	@Override
	public double getMultiplier(Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo,
			EntityDamageByEntityEvent lastDamageCause) {
		return configManager.bonusNoWeapon;
	}

	@Override
	public boolean doesApply(Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo,
			EntityDamageByEntityEvent lastDamageCause) {
		return !extraInfo.hasUsedWeapon() && !extraInfo.isWolfAssist() && !extraInfo.isCrackShotWeaponUsed();
	}

}
