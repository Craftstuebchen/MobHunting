package one.lindegaard.MobHunting.rewards;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.compatibility.ProtocolLibCompat;
import one.lindegaard.MobHunting.compatibility.ProtocolLibHelper;
import one.lindegaard.MobHunting.mobs.MinecraftMob;
import one.lindegaard.MobHunting.util.Misc;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class RewardListeners implements Listener {

    private RewardManager rewardManager;
    private ProtocolLibCompat protocolLibCompat;
    private ProtocolLibHelper protocolLibHelper;
    private CustomItems customItems;
    private ConfigManager configManager;
    private Messages messages;

    public RewardListeners(RewardManager rewardManager, ProtocolLibCompat protocolLibCompat, ProtocolLibHelper protocolLibHelper, CustomItems customItems, ConfigManager configManager, Messages messages) {
        this.rewardManager = rewardManager;
        this.protocolLibCompat = protocolLibCompat;
        this.protocolLibHelper = protocolLibHelper;
        this.customItems = customItems;
        this.configManager = configManager;
        this.messages = messages;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDropReward(PlayerDropItemEvent event) {
        if (event.isCancelled())
            return;
        Item item = event.getItemDrop();
        // ItemStack is = item.getItemStack();
        Player player = event.getPlayer();
        if (Reward.isReward(item)) {
            Reward reward = Reward.getReward(item);
            double money = reward.getMoney();
            if (money == 0) {
                item.setCustomName(ChatColor.valueOf(configManager.dropMoneyOnGroundTextColor)
                        + reward.getDisplayname());
                messages.debug("%s dropped a %s (# of rewards left=%s)", player.getName(), reward.getDisplayname(),
                        rewardManager.getDroppedMoney().size());
            } else {
                String displayName = configManager.dropMoneyOnGroundItemtype.equalsIgnoreCase("ITEM")
                        ? rewardManager.format(money)
                        : reward.getDisplayname() + " (" + rewardManager.format(money) + ")";
                item.setCustomName(
                        ChatColor.valueOf(configManager.dropMoneyOnGroundTextColor) + displayName);
                rewardManager.getDroppedMoney().put(item.getEntityId(), money);
                if (!configManager.dropMoneyOnGroundUseAsCurrency)
                    rewardManager.getEconomy().withdrawPlayer(player, money);
                messages.debug("%s dropped %s money. (# of rewards left=%s)", player.getName(),
                        rewardManager.format(money), rewardManager.getDroppedMoney().size());
                messages.playerActionBarMessage(player, messages.getString("mobhunting.moneydrop", "money",
                        rewardManager.format(money)));
            }
            item.setCustomNameVisible(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDespawnRewardEvent(ItemDespawnEvent event) {
        if (event.isCancelled())
            return;

        if (Reward.isReward(event.getEntity())) {
            if (rewardManager.getDroppedMoney().containsKey(event.getEntity().getEntityId())) {
                rewardManager.getDroppedMoney().remove(event.getEntity().getEntityId());
                messages.debug("The reward was lost - despawned (# of rewards left=%s)",
                        rewardManager.getDroppedMoney().size());
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryPickupRewardEvent(InventoryPickupItemEvent event) {
        if (event.isCancelled())
            return;

        Item item = event.getItem();
        if (!item.hasMetadata(RewardManager.MH_REWARD_DATA))
            return;

        if (configManager.denyHoppersToPickUpMoney
                && event.getInventory().getType() == InventoryType.HOPPER) {
            // Messages.debug("A %s tried to pick up the the reward, but this is
            // disabled in config.yml",
            // event.getInventory().getType());
            event.setCancelled(true);
        } else {
            // Messages.debug("The reward was picked up by %s",
            // event.getInventory().getType());
            if (rewardManager.getDroppedMoney().containsKey(item.getEntityId()))
                rewardManager.getDroppedMoney().remove(item.getEntityId());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMoveOverRewardEvent(PlayerMoveEvent event) {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        if (player.getInventory().firstEmpty() == -1 //&& !player.getCanPickupItems()
                ) {
            for (Entity entity : player.getNearbyEntities(1, 1, 1)) {
                if (!(entity instanceof Item))
                    continue;
                Item item = (Item) entity;
                ItemStack isOnground = item.getItemStack();
                if (Reward.isReward(isOnground) && rewardManager.getDroppedMoney().containsKey(entity.getEntityId())) {
                    Reward rewardOnGround = Reward.getReward(isOnground);
                    double moneyOnGround = rewardOnGround.getMoney();
                    // If not Gringotts
                    if (rewardOnGround.getMoney() != 0) {
                        if (protocolLibCompat.isSupported())
                            protocolLibHelper.pickupMoney(player, entity);
                        if (rewardManager.getDroppedMoney().containsKey(entity.getEntityId()))
                            rewardManager.getDroppedMoney().remove(entity.getEntityId());
                        if (!configManager.dropMoneyOnGroundUseAsCurrency) {
                            rewardManager.depositPlayer(player, moneyOnGround);
                            entity.remove();
                            messages.debug("%s picked up the %s money. (# of rewards left=%s)", player.getName(),
                                    rewardManager.format(rewardOnGround.getMoney()),
                                    rewardManager.getDroppedMoney().size());
                            messages.playerActionBarMessage(player, messages.getString("mobhunting.moneypickup",
                                    "money", rewardManager.format(rewardOnGround.getMoney())));
                        } else {

                            boolean found = false;
                            HashMap<Integer, ? extends ItemStack> slots = player.getInventory()
                                    .all(item.getItemStack().getType());
                            for (int slot : slots.keySet()) {
                                ItemStack is = player.getInventory().getItem(slot);
                                if (Reward.isReward(is)) {
                                    Reward reward = Reward.getReward(is);
                                    if ((rewardOnGround.isBagOfGoldReward() || rewardOnGround.isItemReward())
                                            && reward.getRewardUUID().equals(rewardOnGround.getRewardUUID())) {
                                        ItemMeta im = is.getItemMeta();
                                        Reward newReward = Reward.getReward(is);
                                        newReward.setMoney(newReward.getMoney() + rewardOnGround.getMoney());
                                        im.setLore(newReward.getHiddenLore());
                                        String displayName = configManager.dropMoneyOnGroundItemtype
                                                .equalsIgnoreCase("ITEM")
                                                ? rewardManager.format(newReward.getMoney())
                                                : newReward.getDisplayname() + " (" + rewardManager.format(newReward.getMoney()) + ")";
                                        im.setDisplayName(ChatColor
                                                .valueOf(configManager.dropMoneyOnGroundTextColor)
                                                + displayName);
                                        is.setItemMeta(im);
                                        is.setAmount(1);
                                        event.setCancelled(true);
                                        if (protocolLibCompat.isSupported())
                                            protocolLibHelper.pickupMoney(player, item);
                                        item.remove();
                                        messages.debug("ItemStack in slot %s added value %s, new value %s", slot,
                                                rewardManager.format(rewardOnGround.getMoney()),
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
                                        .equalsIgnoreCase("ITEM")
                                        ? rewardManager.format(rewardOnGround.getMoney())
                                        : rewardOnGround.getDisplayname() + " (" + rewardManager
                                        .format(rewardOnGround.getMoney()) + ")";
                                im.setDisplayName(
                                        ChatColor.valueOf(configManager.dropMoneyOnGroundTextColor)
                                                + displayName);
                                im.setLore(rewardOnGround.getHiddenLore());
                                is.setItemMeta(im);
                                item.setItemStack(is);
                                item.setMetadata(RewardManager.MH_REWARD_DATA,
                                        new FixedMetadataValue(MobHunting.getInstance(), new Reward(rewardOnGround)));
                            }

                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileHitRewardEvent(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        Entity targetEntity = null;
        Iterator<Entity> nearby = projectile.getNearbyEntities(1, 1, 1).iterator();
        while (nearby.hasNext()) {
            targetEntity = nearby.next();
            if (Reward.isReward(targetEntity)) {
                if (rewardManager.getDroppedMoney().containsKey(targetEntity.getEntityId()))
                    rewardManager.getDroppedMoney().remove(targetEntity.getEntityId());
                targetEntity.remove();
                messages.debug("The reward was hit by %s and removed. (# of rewards left=%s)", projectile.getType(),
                        rewardManager.getDroppedMoney().size());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRewardBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;

        ItemStack is = event.getItemInHand();
        Block block = event.getBlockPlaced();
        if (Reward.isReward(is)) {
            Reward reward = Reward.getReward(is);
            if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) {
                reward.setMoney(0);
                messages.learn(event.getPlayer(), messages.getString("mobhunting.learn.no-duplication"));
            }
            if (reward.getMoney() == 0)
                reward.setUniqueId(UUID.randomUUID());
            messages.debug("Placed Reward Block:%s", reward.toString());
            block.setMetadata(RewardManager.MH_REWARD_DATA, new FixedMetadataValue(MobHunting.getInstance(), reward));
            rewardManager.getLocations().put(reward.getUniqueUUID(), reward);
            rewardManager.getReward().put(reward.getUniqueUUID(), block.getLocation());
            rewardManager.saveReward(reward.getUniqueUUID());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRewardBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;

        Block block = event.getBlock();
        if (Reward.hasReward(block)) {
            Reward reward = Reward.getReward(block);
            block.getDrops().clear();
            block.setType(Material.AIR);
            block.removeMetadata(RewardManager.MH_REWARD_DATA, MobHunting.getInstance());
            ItemStack is;
            if (reward.isBagOfGoldReward()) {
                is = customItems.getCustomtexture(reward.getRewardUUID(), reward.getDisplayname(),
                        configManager.dropMoneyOnGroundSkullTextureValue,
                        configManager.dropMoneyOnGroundSkullTextureSignature, reward.getMoney(),
                        reward.getUniqueUUID());
            } else {
                // check if is a Minecraft supported head.
                if (reward.getDisplayname().equalsIgnoreCase(MinecraftMob.Skeleton.getFriendlyName()))
                    is = new ItemStack(Material.SKULL_ITEM, 1, (short) 0);
                else if (reward.getDisplayname().equalsIgnoreCase(MinecraftMob.WitherSkeleton.getFriendlyName()))
                    is = new ItemStack(Material.SKULL_ITEM, 1, (short) 1);
                else if (reward.getDisplayname().equalsIgnoreCase(MinecraftMob.Zombie.getFriendlyName()))
                    is = new ItemStack(Material.SKULL_ITEM, 1, (short) 2);
                else if (reward.getDisplayname().equalsIgnoreCase(MinecraftMob.Creeper.getFriendlyName()))
                    is = new ItemStack(Material.SKULL_ITEM, 1, (short) 4);
                else if (reward.getDisplayname().equalsIgnoreCase(MinecraftMob.EnderDragon.getFriendlyName())) {
                    is = new ItemStack(Material.SKULL_ITEM, 1, (short) 5);
                } else
                    is = customItems.getCustomtexture(reward.getRewardUUID(), reward.getDisplayname(),
                            MinecraftMob.getTexture(reward.getDisplayname()),
                            MinecraftMob.getSignature(reward.getDisplayname()), reward.getMoney(),
                            reward.getUniqueUUID());
                is = rewardManager.setDisplayNameAndHiddenLores(is, reward.getDisplayname(), reward.getMoney(),
                        reward.getRewardUUID());
            }
            Item item = block.getWorld().dropItemNaturally(block.getLocation(), is);
            if (reward.getMoney() == 0)
                item.setCustomName(ChatColor.valueOf(configManager.dropMoneyOnGroundTextColor)
                        + reward.getDisplayname());
            else {
                if (reward.isBagOfGoldReward())
                    item.setCustomName(ChatColor.valueOf(configManager.dropMoneyOnGroundTextColor)
                            + reward.getDisplayname() + " ("
                            + rewardManager.format(reward.getMoney()) + ")");
                else
                    item.setCustomName(ChatColor.valueOf(configManager.dropMoneyOnGroundTextColor)
                            + rewardManager.format(reward.getMoney()));
            }
            item.setCustomNameVisible(true);
            item.setMetadata(RewardManager.MH_REWARD_DATA,
                    new FixedMetadataValue(MobHunting.getInstance(), new Reward(reward.getHiddenLore())));
            if (rewardManager.getLocations().containsKey(reward.getUniqueUUID()))
                rewardManager.getLocations().remove(reward.getUniqueUUID());
            if (rewardManager.getReward().containsKey(reward.getUniqueUUID()))
                rewardManager.getReward().remove(reward.getUniqueUUID());
        }

    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClickReward(InventoryClickEvent event) {
        if (event.isCancelled())
            return;

        // Inventory inv = event.getClickedInventory();
        InventoryAction action = event.getAction();
        // ClickType clickType = event.getClick();
        ItemStack isCurrentSlot = event.getCurrentItem();
        if (isCurrentSlot == null)
            return;
        ItemStack isCursor = event.getCursor();
        Player player = (Player) event.getWhoClicked();
        if ((isCurrentSlot.getType() == Material.SKULL_ITEM
                || isCurrentSlot.getType() == Material.valueOf(configManager.dropMoneyOnGroundItem))
                && isCurrentSlot.getType() == isCursor.getType() && action == InventoryAction.SWAP_WITH_CURSOR) {
            if (Reward.isReward(isCurrentSlot) && Reward.isReward(isCursor)) {
                ItemMeta imCurrent = isCurrentSlot.getItemMeta();
                ItemMeta imCursor = isCursor.getItemMeta();
                Reward reward1 = new Reward(imCurrent.getLore());
                Reward reward2 = new Reward(imCursor.getLore());
                if ((reward1.isBagOfGoldReward() || reward1.isItemReward())
                        && reward1.getRewardUUID().equals(reward2.getRewardUUID())) {
                    reward2.setMoney(reward1.getMoney() + reward2.getMoney());
                    imCursor.setLore(reward2.getHiddenLore());
                    imCursor.setDisplayName(ChatColor.valueOf(configManager.dropMoneyOnGroundTextColor)
                            + (configManager.dropMoneyOnGroundItemtype.equalsIgnoreCase("ITEM")
                            ? rewardManager.format(reward2.getMoney())
                            : reward2.getDisplayname() + " ("
                            + rewardManager.format(reward2.getMoney()) + ")"));
                    isCursor.setItemMeta(imCursor);
                    isCurrentSlot.setAmount(0);
                    isCurrentSlot.setType(Material.AIR);
                    messages.debug("%s merged two rewards", player.getName());
                }
            }

        } else if (isCursor.getType() == Material.AIR && (isCurrentSlot.getType() == Material.SKULL_ITEM
                || isCurrentSlot.getType() == Material.valueOf(configManager.dropMoneyOnGroundItem))
                && action == InventoryAction.PICKUP_HALF && !event.isShiftClick()) {
            if (Reward.isReward(isCurrentSlot)) {
                Reward reward = Reward.getReward(isCurrentSlot);
                if (reward.isBagOfGoldReward() || reward.isItemReward()) {
                    double money = reward.getMoney() / 2;
                    if (Misc.floor(money) >= configManager.minimumReward) {
                        event.setCancelled(true);
                        if (configManager.dropMoneyOnGroundItemtype.equalsIgnoreCase("ITEM")) {
                            isCurrentSlot = rewardManager.setDisplayNameAndHiddenLores(isCurrentSlot.clone(),
                                    reward.getDisplayname(), Misc.ceil(money), reward.getRewardUUID());
                        } else {
                            isCurrentSlot = customItems.getCustomtexture(reward.getRewardUUID(),
                                    reward.getDisplayname(),
                                    configManager.dropMoneyOnGroundSkullTextureValue,
                                    configManager.dropMoneyOnGroundSkullTextureSignature,
                                    Misc.ceil(money), UUID.randomUUID());
                        }

                        event.setCurrentItem(isCurrentSlot);

                        if (configManager.dropMoneyOnGroundItemtype.equalsIgnoreCase("ITEM")) {
                            isCursor = rewardManager.setDisplayNameAndHiddenLores(isCurrentSlot.clone(),
                                    reward.getDisplayname(), Misc.floor(money), reward.getRewardUUID());
                        } else {
                            isCursor = customItems.getCustomtexture(reward.getRewardUUID(), reward.getDisplayname(),
                                    configManager.dropMoneyOnGroundSkullTextureValue,
                                    configManager.dropMoneyOnGroundSkullTextureSignature,
                                    Misc.floor(money), UUID.randomUUID());
                        }
                        event.setCursor(isCursor);

                        messages.debug("%s halfed a reward in two (%s,%s)", player.getName(),
                                rewardManager.format(Misc.floor(money)),
                                rewardManager.format(Misc.ceil(money)));
                    }
                }
            }
        }
    }

}
