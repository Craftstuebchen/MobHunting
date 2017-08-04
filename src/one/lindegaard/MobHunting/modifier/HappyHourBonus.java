package one.lindegaard.MobHunting.modifier;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.DamageInformation;
import one.lindegaard.MobHunting.HuntData;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.commands.HappyHourCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class HappyHourBonus extends IModifier {

	private HappyHourCommand happyHourCommand;

	public HappyHourBonus(ConfigManager configManager,Messages messages, HappyHourCommand happyHourCommand) {
		super(configManager,messages);
		this.happyHourCommand = happyHourCommand;
	}

	@Override
	public String getName() {
		return ChatColor.LIGHT_PURPLE + messages.getString("bonus.happyhour.name");
	}

	@Override
	public double getMultiplier(Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo,
			EntityDamageByEntityEvent lastDamageCause) {
		return happyHourCommand.multiplier;
	}

	@Override
	public boolean doesApply(Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo,
			EntityDamageByEntityEvent lastDamageCause) {
		return happyHourCommand.minutesLeft != 0;
	}

}
