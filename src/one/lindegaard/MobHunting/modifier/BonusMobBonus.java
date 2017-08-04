package one.lindegaard.MobHunting.modifier;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.DamageInformation;
import one.lindegaard.MobHunting.HuntData;
import one.lindegaard.MobHunting.Messages;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class BonusMobBonus extends IModifier {

	public BonusMobBonus(ConfigManager configManager,Messages messages) {
		super(configManager,messages);
	}

	@Override
	public String getName() {
		return messages.getString("bonus.bonusmob.name");
	}

	@Override
	public double getMultiplier(Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo,
			EntityDamageByEntityEvent lastDamageCause) {
		return configManager.bonusBonusMob;
	}

	@Override
	public boolean doesApply(Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo,
			EntityDamageByEntityEvent lastDamageCause) {
		return deadEntity.hasMetadata("MH:hasBonus");
	}

}
