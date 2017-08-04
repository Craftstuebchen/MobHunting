package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.events.MobHuntKillEvent;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class Electrifying extends Achievement implements Listener {

	public Electrifying(ConfigManager configManager, AchievementManager achievementManager, ExtendedMobManager extendedMobManager,Messages messages) {
		super(configManager, achievementManager, extendedMobManager, messages);
	}

	@Override
	public String getName() {
		return messages.getString("achievements.electrifying.name");
	}

	@Override
	public String getID() {
		return "electrifying";
	}

	@Override
	public String getDescription() {
		return messages.getString("achievements.electrifying.description");
	}

	@Override
	public double getPrize() {
		return configManager.specialCharged;
	}

	@EventHandler
	public void onKill(MobHuntKillEvent event) {
		if (event.getKilledEntity() instanceof Creeper && ((Creeper) event.getKilledEntity()).isPowered()
				&& configManager.getBaseKillPrize(event.getKilledEntity()) > 0)
			achievementManager.awardAchievement(this, event.getPlayer(),
					extendedMobManager.getExtendedMobFromEntity(event.getKilledEntity()));
	}

	@Override
	public String getPrizeCmd() {
		return configManager.specialChargedCmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return configManager.specialChargedCmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1);
		SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
		skullMeta.setOwner("MHF_Creeper");
		skull.setItemMeta(skullMeta);
		return skull;
	}
}
