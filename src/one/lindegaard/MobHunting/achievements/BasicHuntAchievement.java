package one.lindegaard.MobHunting.achievements;

import org.bukkit.inventory.ItemStack;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.mobs.MinecraftMob;

public class BasicHuntAchievement implements ProgressAchievement {
	private MinecraftMob mType;

	public BasicHuntAchievement(MinecraftMob entity) {
		mType = entity;
	}

	@Override
	public String getName() {
		return Messages.getString("achievements.hunter.1.name", "mob",
				mType.getFriendlyName());
	}

	@Override
	public String getID() {
		return "hunting-level1-" + mType.name().toLowerCase();
	}

	@Override
	public String getDescription() {
		return Messages.getString("achievements.hunter.1.description", "count",
				getMaxProgress(), "mob", mType.getFriendlyName());
	}

	@Override
	public double getPrize() {
		return MobHunting.getConfigManager().specialHunter1;
	}

	@Override
	public int getMaxProgress() {
		return mType.getMax();
	}

	@Override
	public String inheritFrom() {
		return null;
	}
	
	@Override
	public String nextLevelId() {
		return "hunting-level2-" + mType.name().toLowerCase();
	}

	@Override
	public String getPrizeCmd() {
		return MobHunting.getConfigManager().specialHunter1Cmd;
	}

	@Override
	public String getPrizeCmdDescription() {
		return MobHunting.getConfigManager().specialHunter1CmdDesc;
	}

	@Override
	public ItemStack getSymbol() {
		return getExtendedMobType().getCustomHead(1,0);
	}
	
	@Override
	public MinecraftMob getExtendedMobType() {
		return mType;
	}
}
