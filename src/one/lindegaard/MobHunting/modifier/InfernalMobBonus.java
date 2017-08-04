package one.lindegaard.MobHunting.modifier;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.DamageInformation;
import one.lindegaard.MobHunting.HuntData;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.compatibility.InfernalMobsCompat;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;

public class InfernalMobBonus extends IModifier {

	public InfernalMobBonus(ConfigManager configManager) {
		super(configManager);
	}

	@Override
	public String getName() {
		return ChatColor.AQUA + Messages.getString("bonus.infernalmob.name");
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getMultiplier(Entity entity, Player killer, HuntData data, DamageInformation extraInfo,
			EntityDamageByEntityEvent lastDamageCause) {
		double mul = 1;
		if (InfernalMobsCompat.isSupported()) {
			if (entity.hasMetadata(InfernalMobsCompat.MH_INFERNALMOBS)) {
				ArrayList<String> list = new ArrayList<>();
				if (entity.getMetadata(InfernalMobsCompat.MH_INFERNALMOBS).get(0).value() instanceof ArrayList<?>)
					list = (ArrayList<String>) entity.getMetadata(InfernalMobsCompat.MH_INFERNALMOBS).get(0).value();
				mul = Math.pow(configManager.multiplierPerInfernalLevel, list.size());
			}
		}
		return mul;
	}

	@Override
	public boolean doesApply(Entity entity, Player killer, HuntData data, DamageInformation extraInfo,
			EntityDamageByEntityEvent lastDamageCause) {
		return InfernalMobsCompat.isInfernalMob(entity);
	}
}
