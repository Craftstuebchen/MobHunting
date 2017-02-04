package one.lindegaard.MobHunting.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
//import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.mobs.MinecraftMob;
import one.lindegaard.MobHunting.util.Misc;

public class HeadCommand implements ICommand, Listener {

	public static final String MH_HEAD = "MH:Head";
	public static final String MH_REWARD = "MobHunting Reward";

	public HeadCommand(MobHunting instance) {
		Bukkit.getServer().getPluginManager().registerEvents(this, instance);
	}

	// Used case
	// /mh head give [toPlayer] [mobname|playername] [displayname] [amount] - to
	// give a head to a player.
	// /mh head rename [displayname] - to rename the head holding in the hand.

	@Override
	public String getName() {
		return "head";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "ph", "playerhead", "heads", "mobhead", "spawn" };
	}

	@Override
	public String getPermission() {
		return "mobhunting.head";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] {
				ChatColor.GOLD + label + ChatColor.GREEN + " give" + " [toPlayername] [playername|mobname]"
						+ ChatColor.YELLOW + " [displayname] [amount] [silent]" + ChatColor.WHITE + " - to give a head",
				ChatColor.GOLD + label + ChatColor.GREEN + " rename [new displayname]" + ChatColor.WHITE
						+ " - to rename the head in players name" };
	}

	@Override
	public String getDescription() {
		return Messages.getString("mobhunting.commands.head.description");
	}

	@Override
	public boolean canBeConsole() {
		return true;
	}

	@Override
	public boolean canBeCommandBlock() {
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		// /mh head give [toPlayername] [mobname|playername] [displayname]
		// [amount]
		if (args.length >= 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("spawn"))) {
			if (args.length >= 3) {
				OfflinePlayer offlinePlayer = null, toPlayer = null;
				String displayName;
				int amount = 1;

				// get toPlayerName
				toPlayer = Bukkit.getOfflinePlayer(args[1]);
				if (toPlayer == null || !toPlayer.isOnline()) {
					sender.sendMessage(Messages.getString("mobhunting.commands.head.online", "playername", args[1]));
					return true;
				}

				// get MobType / PlayerName
				MinecraftMob mob = MinecraftMob.getExtendedMobType(args[2]);
				if (mob == null) {
					offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
					if (offlinePlayer != null) {
						mob = MinecraftMob.PvpPlayer;
					} else {
						sender.sendMessage(
								Messages.getString("mobhunting.commands.head.unknown_name", "playername", args[2]));
						return true;
					}
				}
				// get displayname
				if (args.length >= 4) {
					displayName = args[3].replace("_", " ");
				} else {
					if (mob != MinecraftMob.PvpPlayer)
						displayName = mob.getDisplayName().replace("_", " ");
					else
						displayName = offlinePlayer.getName();
				}
				// get amount
				if (args.length >= 5) {
					try {
						amount = Integer.valueOf(args[4]);
					} catch (NumberFormatException e) {
						sender.sendMessage(
								Messages.getString("mobhunting.commands.base.not_a_number", "number", args[4]));
						return false;
					}
				}
				// silent
				boolean silent = false;
				if (args.length >= 6 && (args[5].equalsIgnoreCase("silent") || args[5].equalsIgnoreCase("true")
						|| args[5].equalsIgnoreCase("1"))) {
					silent = true;
				}
				if (mob != null) {
					if (Misc.isMC18OrNewer()) {
						// Use GameProfile
						((Player) toPlayer).getWorld().dropItem(((Player) toPlayer).getLocation(),
								mob.getHead(displayName));
					} else {
						String cmdString = mob.getCommandString().replace("{player}", toPlayer.getName())
								.replace("{displayname}", displayName).replace("{lore}", MH_REWARD)
								.replace("{playerid}", mob.getPlayerUUID())
								.replace("{texturevalue}", mob.getTextureValue())
								.replace("{amount}", String.valueOf(amount)).replace("{playername}",
										offlinePlayer != null ? offlinePlayer.getName() : mob.getPlayerProfile());
						Messages.debug("%s Cmd=%s", mob.getDisplayName(), cmdString);
						Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), cmdString);
					}
					if (toPlayer.isOnline() && !silent) {
						((Player) toPlayer).sendMessage("You got a head of " + displayName + ".");
					}

				}
			}

			return true;

		} else if (args.length > 1 && (args[0].equalsIgnoreCase("rename"))) {
			// mh head rename [displayname] - to rename the head in hand.
			if (sender instanceof Player) {
				Player player = (Player) sender;
				ItemStack itemInHand = player.getItemInHand();
				if (itemInHand.hasItemMeta() && itemInHand.getItemMeta().hasLore()
						&& itemInHand.getItemMeta().getLore().get(0).equals(MH_REWARD)) {
					String displayname = "";
					for (int i = 1; i < args.length; i++) {
						if (i != (args.length - 1))
							displayname = displayname + args[i] + " ";
						else
							displayname = displayname + args[i];
					}
					ItemMeta im = itemInHand.getItemMeta();
					im.setDisplayName(displayname);
					itemInHand.setItemMeta(im);
				} else {
					sender.sendMessage(Messages.getString("mobhunting.commands.head.headmustbeinhand"));
				}
			} else {
				sender.sendMessage("You can only rename heads ingame.");
			}
			return true;
		}
		// show help
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
		ArrayList<String> items = new ArrayList<String>();
		if (args.length == 0) {
			items.add(" give");
			items.add(" rename");
		} else if (args.length == 1) {
			if (items.isEmpty()) {
				items.add("give");
				items.add("rename");
			}

		} else if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("spawn"))) {
			String partial = args[1].toLowerCase();
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getName().toLowerCase().startsWith(partial))
					items.add(player.getName());
			}
		} else if ((args.length == 3 && args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("spawn"))) {
			String partial = args[2].toLowerCase();
			for (MinecraftMob mob : MinecraftMob.values()) {
				if (mob.getFriendlyName().toLowerCase().startsWith(partial)
						|| mob.getDisplayName().toLowerCase().startsWith(partial))
					items.add(mob.getFriendlyName().replace(" ", "_"));
			}
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getName().toLowerCase().startsWith(partial))
					items.add(player.getName());
			}
		}
		return items;
	}

	@EventHandler
	public void PickupItem(PlayerPickupItemEvent event) {
		if (event.isCancelled())
			return;
		Item item = event.getItem();
		if (item.hasMetadata(HeadCommand.MH_HEAD)) {
			String displayName = item.getMetadata(HeadCommand.MH_HEAD).get(0).asString();
			Messages.debug("You picked up a MH Head DisplayName=%s", displayName);
			ItemMeta im = item.getItemStack().getItemMeta();
			im.setDisplayName(displayName);
			ArrayList<String> lore = new ArrayList<String>();
			lore.add(HeadCommand.MH_REWARD);
			im.setLore(lore);
			event.getItem().getItemStack().setItemMeta(im);
		}
	}

	@EventHandler
	public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
		if (event.isCancelled())
			return;
		Item item = event.getItemDrop();
		if (item.getItemStack().hasItemMeta() && item.getItemStack().getItemMeta().hasLore()
				&& item.getItemStack().getItemMeta().getLore().get(0).equals(MH_REWARD)) {
			String displayName = item.getItemStack().getItemMeta().getDisplayName();
			Messages.debug("You dropped a MH Head DisplayName=%s", displayName);
			ItemMeta im = item.getItemStack().getItemMeta();
			im.setDisplayName(displayName);
			ArrayList<String> lore = new ArrayList<String>();
			lore.add(HeadCommand.MH_REWARD);
			im.setLore(lore);
			event.getItemDrop().setMetadata(MH_HEAD, new FixedMetadataValue(MobHunting.getInstance(),
					item.getItemStack().getItemMeta().getDisplayName()));
			event.getItemDrop().getItemStack().setItemMeta(im);
		}
	}

	@EventHandler
	public void onInventoryPickUp(InventoryPickupItemEvent event) {
		if (event.isCancelled())
			return;
		Item item = event.getItem();
		if (item.hasMetadata(MH_HEAD) && event.getInventory().getType() != InventoryType.PLAYER) {
			String displayName = item.getMetadata(MH_HEAD).get(0).asString();
			Messages.debug("A Inventory picked up a MH Head DisplayName=%s", displayName);
			ItemMeta im = item.getItemStack().getItemMeta();
			im.setDisplayName(displayName);
			ArrayList<String> lore = new ArrayList<String>();
			lore.add(MH_REWARD);
			im.setLore(lore);
			event.getItem().getItemStack().setItemMeta(im);
		}
	}

	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (event.isCancelled())
			return;
		if (event.getBlock().hasMetadata(MH_HEAD)) {
			String displayName = event.getBlock().getMetadata(MH_HEAD).get(0).asString();
			Messages.debug("You broke a MH head Displayname=%s", displayName);
			Iterator<ItemStack> itr = event.getBlock().getDrops().iterator();
			while (itr.hasNext()) {
				ItemStack is = itr.next();
				ItemStack is2 = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
				itr.remove();

				ItemMeta im = is.getItemMeta();
				ArrayList<String> lore = new ArrayList<String>();
				lore.add(MH_REWARD);
				im.setLore(lore);
				im.setDisplayName(displayName);
				is2.setItemMeta(im);

				event.getBlock().getLocation().getWorld().dropItem(event.getBlock().getLocation(), is2);
				event.getBlock().setType(Material.AIR);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onBlockPlaceEvent(BlockPlaceEvent event) {
		if (event.isCancelled())
			return;
		if (event.getItemInHand() != null && event.getItemInHand().hasItemMeta()) {
			ItemMeta im = event.getItemInHand().getItemMeta();
			if (im.hasLore() && im.getLore().get(0).equalsIgnoreCase(HeadCommand.MH_REWARD)
					&& !im.getDisplayName().isEmpty()) {
				event.getBlockPlaced().setMetadata(HeadCommand.MH_HEAD,
						new FixedMetadataValue(MobHunting.getInstance(), im.getDisplayName()));
				Messages.debug("You placed a MH Head DisplayName=%s", im.getDisplayName());
			}
		}
	}

	/**
	 * @EventHandler public void onPlayerInteractEvent(PlayerInteractEvent
	 *               event) { if (event.getItem() != null) if
	 *               (event.getMaterial() == Material.SKULL_ITEM ||
	 *               event.getMaterial() == Material.SKULL) { if
	 *               (event.getItem().hasItemMeta() &&
	 *               event.getItem().getItemMeta().hasLore() &&
	 *               event.getItem().getItemMeta().getLore().get(0).
	 *               equalsIgnoreCase(HeadCommand.MH_REWARD)) { String
	 *               displayName =
	 *               event.getItem().getItemMeta().getDisplayName();
	 *               Messages.debug("You hit a MH Head DisplayName=%s",
	 *               displayName); } } }
	 **/

}
