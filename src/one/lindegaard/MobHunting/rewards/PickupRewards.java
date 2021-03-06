package one.lindegaard.MobHunting.rewards;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.compatibility.ProtocolLibCompat;
import one.lindegaard.MobHunting.compatibility.ProtocolLibHelper;
import org.bukkit.ChatColor;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;

public class PickupRewards {


    private ProtocolLibCompat protocolLibCompat;
    private ProtocolLibHelper protocolLibHelper;
    private RewardManager rewardManager;
    private ConfigManager configManager;
    private Messages messages;

    public PickupRewards(ProtocolLibCompat protocolLibCompat, ProtocolLibHelper protocolLibHelper, RewardManager rewardManager, ConfigManager configManager, Messages messages) {
        this.protocolLibCompat = protocolLibCompat;
        this.protocolLibHelper = protocolLibHelper;
        this.rewardManager = rewardManager;
        this.configManager = configManager;
        this.messages = messages;
    }


    public void rewardPlayer(Player player, Item item, CallBack callBack) {

        if (Reward.isReward(item)) {
            Reward reward = Reward.getReward(item);
            // If not Gringotts
            if (reward.getMoney() != 0)
                if (!configManager.dropMoneyOnGroundUseAsCurrency) {
                    rewardManager.depositPlayer(player, reward.getMoney());
                    if (protocolLibCompat.isSupported())
                        protocolLibHelper.pickupMoney(player, item);
                    item.remove();
                    callBack.setCancelled(true);
                    messages.playerActionBarMessage(player, messages.getString("mobhunting.moneypickup", "money",
                            rewardManager.format(reward.getMoney())));
                } else {
                    boolean found = false;
                    HashMap<Integer, ? extends ItemStack> slots = player.getInventory()
                            .all(item.getItemStack().getType());
                    for (int slot : slots.keySet()) {
                        ItemStack is = player.getInventory().getItem(slot);
                        if (Reward.isReward(is)) {
                            Reward rewardInSlot = Reward.getReward(is);
                            if ((reward.isBagOfGoldReward() || reward.isItemReward())
                                    && rewardInSlot.getRewardUUID().equals(reward.getRewardUUID())) {
                                ItemMeta im = is.getItemMeta();
                                Reward newReward = Reward.getReward(is);
                                newReward.setMoney(newReward.getMoney() + reward.getMoney());
                                im.setLore(newReward.getHiddenLore());
                                String displayName = configManager.dropMoneyOnGroundItemtype
                                        .equalsIgnoreCase("ITEM")
                                        ? rewardManager.format(newReward.getMoney())
                                        : newReward.getDisplayname() + " ("
                                        + rewardManager.format(newReward.getMoney())
                                        + ")";
                                im.setDisplayName(
                                        ChatColor.valueOf(configManager.dropMoneyOnGroundTextColor)
                                                + displayName);
                                is.setItemMeta(im);
                                is.setAmount(1);
                                callBack.setCancelled(true);
                                if (protocolLibCompat.isSupported())
                                    protocolLibHelper.pickupMoney(player, item);
                                item.remove();
                                messages.debug("Added %s to item in slot %s, new value is %s",
                                        rewardManager.format(reward.getMoney()), slot,
                                        rewardManager.format(newReward.getMoney()));
                                found = true;
                                break;
                            }
                        }
                    }

                    if (!found) {
                        ItemStack is = item.getItemStack();
                        ItemMeta im = is.getItemMeta();
                        String displayName = configManager.dropMoneyOnGroundItemtype
                                .equalsIgnoreCase("ITEM") ? rewardManager.format(reward.getMoney())
                                : reward.getDisplayname() + " ("
                                + rewardManager.format(reward.getMoney()) + ")";
                        im.setDisplayName(
                                ChatColor.valueOf(configManager.dropMoneyOnGroundTextColor)
                                        + displayName);
                        im.setLore(reward.getHiddenLore());
                        is.setItemMeta(im);
                        item.setItemStack(is);
                        item.setMetadata(RewardManager.MH_REWARD_DATA,
                                new FixedMetadataValue(MobHunting.getInstance(), new Reward(reward)));
                    }
                }
            if (rewardManager.getDroppedMoney().containsKey(item.getEntityId()))
                rewardManager.getDroppedMoney().remove(item.getEntityId());
            if (reward.getMoney() == 0)
                messages.debug("%s picked up a %s (# of rewards left=%s)", player.getName(),
                        reward.getDisplayname(), rewardManager.getDroppedMoney().size());
            else
                messages.debug("%s picked up a %s with a value:%s (# of rewards left=%s)", player.getName(),
                        reward.getDisplayname(), rewardManager.format(reward.getMoney()),
                        rewardManager.getDroppedMoney().size());

        }
    }


    public interface CallBack {

        void setCancelled(boolean canceled);

    }


}
