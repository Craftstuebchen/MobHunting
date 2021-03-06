package one.lindegaard.MobHunting.modifier;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.DamageInformation;
import one.lindegaard.MobHunting.HuntData;
import one.lindegaard.MobHunting.Messages;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Iterator;
import java.util.Map.Entry;

public class RankBonus extends IModifier {

	public RankBonus(ConfigManager configManager) {
		super(configManager);
	}

	@Override
	public String getName() {
		return ChatColor.GRAY + Messages.getString("bonus.rank.name");
	}

	@Override
	public double getMultiplier(Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo,
			EntityDamageByEntityEvent lastDamageCause) {
		if (killer!=null && !killer.isOp()) {
			Iterator<Entry<String, String>> ranks = configManager.rankMultiplier.entrySet().iterator();
			double mul = 0;
			while (ranks.hasNext()) {
				Entry<String, String> rank = ranks.next();
				if (!rank.getKey().equalsIgnoreCase("mobhunting")
						&& !rank.getKey().equalsIgnoreCase("mobhunting.multiplier")) {
					if (killer.hasPermission(rank.getKey())) {
						mul = (Double.valueOf(rank.getValue()) > mul) ? Double.valueOf(rank.getValue()) : mul;
					}
				}
			}
			mul = (mul == 0) ? 1 : mul;
			return mul;
		} else if (configManager.rankMultiplier.containsKey("mobhunting.multiplier.op"))
			return Double.valueOf(configManager.rankMultiplier.get("mobhunting.multiplier.op"));
		return 1;
	}

	@Override
	public boolean doesApply(Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo,
			EntityDamageByEntityEvent lastDamageCause) {
		if (killer!=null && !killer.isOp()) {
			Iterator<Entry<String, String>> ranks = configManager.rankMultiplier.entrySet().iterator();
			boolean hasRank = false;
			while (ranks.hasNext()) {
				Entry<String, String> rank = ranks.next();
				if (!rank.getKey().equalsIgnoreCase("mobhunting")
						&& !rank.getKey().equalsIgnoreCase("mobhunting.multiplier")) {
					if (killer.hasPermission(rank.getKey())) {
						// Messages.debug("RankMultiplier Key=%s Value=%s",
						// rank.getKey(), rank.getValue());
						hasRank = true;
					}
				}
			}
			return hasRank;
		} else if (configManager.rankMultiplier.containsKey("mobhunting.multiplier.op")) {
			Messages.debug("RankMultiplier Key=mobhunting.multiplier.op Value=%s Player is OP",
					configManager.rankMultiplier.get("mobhunting.multiplier.op"));
			return true;
		}
		Messages.debug("%s has no Rank Multiplier", killer.getName());
		return false;
	}
}
