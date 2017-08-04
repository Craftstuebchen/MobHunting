package one.lindegaard.MobHunting.modifier;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.DamageInformation;
import one.lindegaard.MobHunting.HuntData;
import one.lindegaard.MobHunting.Messages;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public abstract class IModifier {

	protected ConfigManager configManager;
	protected Messages messages;

	public IModifier(ConfigManager configManager,Messages messages) {
		this.configManager = configManager;
		this.messages=messages;
	}

	public abstract String getName();

	public abstract double getMultiplier(Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo,
						 EntityDamageByEntityEvent lastDamageCause);

	public abstract boolean doesApply(Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo,
					  EntityDamageByEntityEvent lastDamageCause);
}
