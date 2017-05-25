package one.lindegaard.MobHunting.achievements;

import org.bukkit.inventory.ItemStack;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.mobs.MinecraftMob;

public class SixthHuntAchievement implements ProgressAchievement {

	private MinecraftMob mType;

	public SixthHuntAchievement(MinecraftMob entity) {
		mType = entity;
	}

	@Override
	public String getName() {
		return Messages.getString("achievements.hunter.6.name", "mob", mType.getFriendlyName());
	}

	@Override
	public String getID() {
		return "hunting-level6-" + mType.name().toLowerCase();
	}

	@Override
	public String getDescription() {
		return Messages.getString("achievements.hunter.6.description", "count", getMaxProgress(), "mob",
				mType.getFriendlyName());
	}

	@Override
	public double getPrize() {
		return MobHunting.getConfigManager().specialHunter6;
	}

	@Override
	public int getMaxProgress() {
		return mType.getMax() * 50;
	}

	@Override
	public String inheritFrom() {
		return "hunting-level5-" + mType.name().toLowerCase(); 
	}
	
	@Override
	public String nextLevelId() {
		return "hunting-level7-" + mType.name().toLowerCase();
	}

	@Override
	public String getPrizeCmd() {
		return MobHunting.getConfigManager().specialHunter6Cmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return MobHunting.getConfigManager().specialHunter6CmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return getExtendedMobType().getCustomHead(6,0);
	}
	
	@Override
	public MinecraftMob getExtendedMobType() {
		return mType;
	}
}
