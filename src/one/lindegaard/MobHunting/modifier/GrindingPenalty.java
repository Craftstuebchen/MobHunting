package one.lindegaard.MobHunting.modifier;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.DamageInformation;
import one.lindegaard.MobHunting.HuntData;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.grinding.GrindingManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class GrindingPenalty extends IModifier
{

	private GrindingManager grindingManager;
	public GrindingPenalty(ConfigManager configManager, GrindingManager grindingManager) {
		super(configManager);
		this.grindingManager = grindingManager;
	}

	@Override
	public String getName()
	{
		return ChatColor.RED + Messages.getString("penalty.grinding.name");  //$NON-NLS-1$
	}

	@Override
	public double getMultiplier(Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo, EntityDamageByEntityEvent lastDamageCause)
	{
		return data.getDampnerMultiplier();
	}

	@Override
	public boolean doesApply(Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo, EntityDamageByEntityEvent lastDamageCause )
	{
		if(configManager.grindingDetectionEnabled && !grindingManager.isWhitelisted(deadEntity.getLocation()))
			return data.getDampnerMultiplier() < 1;
		return false;
	}

}
