package one.lindegaard.MobHunting.modifier;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.DamageInformation;
import one.lindegaard.MobHunting.HuntData;
import one.lindegaard.MobHunting.Messages;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class FriendleFireBonus extends IModifier
{
	public FriendleFireBonus(ConfigManager configManager) {
		super(configManager);
	}

	@Override
	public String getName()
	{
		return ChatColor.DARK_GREEN + Messages.getString("bonus.friendlyfire.name"); //$NON-NLS-1$
	}

	@Override
	public double getMultiplier( Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo, EntityDamageByEntityEvent lastDamageCause )
	{
		return configManager.bonusFriendlyFire;
	}

	@Override
	public boolean doesApply(Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo, EntityDamageByEntityEvent lastDamageCause )
	{
		if(lastDamageCause == null)
			return false;
		if(lastDamageCause.getDamager() instanceof Monster || (lastDamageCause.getDamager() instanceof Projectile && (((Projectile)lastDamageCause.getDamager()).getShooter() instanceof Monster || ((Projectile)lastDamageCause.getDamager()).getShooter() instanceof Ghast)))
			return true;
		return false;
	}

}
