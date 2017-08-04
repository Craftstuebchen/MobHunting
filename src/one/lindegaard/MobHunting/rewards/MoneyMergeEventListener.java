package one.lindegaard.MobHunting.rewards;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import org.bukkit.ChatColor;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class MoneyMergeEventListener implements Listener {


	private RewardManager rewardManager;
	private ConfigManager configManager;
    private Messages messages;

    public MoneyMergeEventListener(RewardManager rewardManager, ConfigManager configManager, Messages messages) {
		this.rewardManager = rewardManager;
		this.configManager = configManager;
        this.messages = messages;
    }


	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=false)
	public void onMoneyMergeEvent(ItemMergeEvent event) {
		// OBS: ItemMergeEvent does only exist in MC1.8 and newer

		if (event.isCancelled())
			return;

		Item item1 = event.getEntity();
		Item item2 = event.getTarget();
		ItemStack is2 = item2.getItemStack();
		if (Reward.isReward(item1) && Reward.isReward(item2)) {
			Reward reward1 = Reward.getReward(item1);
			Reward reward2 = Reward.getReward(item2);
			if (reward1.getRewardUUID().equals(reward2.getRewardUUID())
					&& (reward1.isBagOfGoldReward() || reward1.isItemReward())) {
				if (reward1.getMoney() + reward2.getMoney() != 0) {
					reward2.setMoney(reward1.getMoney() + reward2.getMoney());
					ItemMeta im = is2.getItemMeta();
					is2.setItemMeta(im);
					is2.setAmount(0);
					item2.setItemStack(is2);
					String displayName = configManager.dropMoneyOnGroundItemtype
							.equalsIgnoreCase("ITEM") ? rewardManager.format(reward2.getMoney())
									: reward2.getDisplayname() + "("
											+ rewardManager.format(reward2.getMoney()) + ")";
					item2.setCustomName(
							ChatColor.valueOf(configManager.dropMoneyOnGroundTextColor) + displayName);
					item2.setCustomNameVisible(true);
					item2.setMetadata(RewardManager.MH_REWARD_DATA,
							new FixedMetadataValue(MobHunting.getInstance(), new Reward(reward2)));
					messages.debug("Rewards merged - new value=%s",
							rewardManager.format(reward2.getMoney()));
				}
				if (rewardManager.getDroppedMoney().containsKey(item1.getEntityId()))
                    rewardManager.getDroppedMoney().remove(item1.getEntityId());
			}
		}
	}
}
