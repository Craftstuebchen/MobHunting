package one.lindegaard.MobHunting.modifier;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.DamageInformation;
import one.lindegaard.MobHunting.HuntData;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.compatibility.ConquestiaMobsCompat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ConquestiaBonus extends IModifier {

	public ConquestiaBonus(ConfigManager configManager) {
		super(configManager);
	}

	@Override
	public String getName() {
		return Messages.getString("bonus.conquestiamobs.name");
	}

	@Override
	public double getMultiplier(Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo,
			EntityDamageByEntityEvent lastDamageCause) {
		Messages.debug("ConquestiaMob total multiplier = %s", Math.pow(
				configManager.mulitiplierPerLevel, ConquestiaMobsCompat.getCqLevel(deadEntity)-1));
		return Math.pow(configManager.mulitiplierPerLevel,
				ConquestiaMobsCompat.getCqLevel(deadEntity)-1);
	}

	@Override
	public boolean doesApply(Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo,
			EntityDamageByEntityEvent lastDamageCause) {
		Messages.debug("%s killed a ConquestiaMob %s level %s", killer.getName(), deadEntity.getType(),
				ConquestiaMobsCompat.getCqLevel(deadEntity));
		return deadEntity.hasMetadata(ConquestiaMobsCompat.MH_CONQUESTIAMOBS);
	}

}
