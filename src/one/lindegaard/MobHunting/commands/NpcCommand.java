package one.lindegaard.MobHunting.commands;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.StatType;
import one.lindegaard.MobHunting.compatibility.CitizensCompat;
import one.lindegaard.MobHunting.compatibility.CompatibilityManager;
import one.lindegaard.MobHunting.npc.MasterMobHunter;
import one.lindegaard.MobHunting.npc.MasterMobHunterManager;
import one.lindegaard.MobHunting.npc.MasterMobHunterTrait;
import one.lindegaard.MobHunting.storage.TimePeriod;
import one.lindegaard.MobHunting.util.Misc;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NpcCommand implements ICommand, Listener {

	public NpcCommand(MobHunting instance) {
		Bukkit.getPluginManager().registerEvents(this, instance);
	}

	// Used case (???)
	// /mh npc create <stat type> <period> <number>
	// /mh npc remove
	// /mh npc update
	// /mh npc spawn
	// /mh npc despawn
	// /mh npc select
	// /mh npc tphere
	// /mh npc sethome

	@Override
	public String getName() {
		return "npc";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "citizens" };
	}

	@Override
	public String getPermission() {
		return "mobhunting.npc";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] {
				ChatColor.GOLD + label + ChatColor.GREEN + " create <stattype> <period> <number>" + ChatColor.WHITE
						+ " - to create a MasterMobHunter NPC",
				ChatColor.GOLD + label + ChatColor.GREEN + " remove" + ChatColor.WHITE
						+ " - to remove the selected MasterMobHunter",
				ChatColor.GOLD + label + ChatColor.GREEN + " update" + ChatColor.WHITE
						+ " - to update selected the MasterMobHunter NPC" };
	}

	@Override
	public String getDescription() {
		return Messages.getString("mobhunting.commands.npc.description");
	}

	@Override
	public boolean canBeConsole() {
		return false;
	}

	@Override
	public boolean canBeCommandBlock() {
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {

		String[] subcmds = { "create", "remove", "select", "spawn", "despawn", "update", "tphere", "sethome" };
		ArrayList<String> items = new ArrayList<String>();
		if (CompatibilityManager.isPluginLoaded(CitizensCompat.class)) {
			if (args.length == 1) {
				for (String cmd : subcmds)
					items.add(cmd);
			} else if (args.length == 2) {
				if (args[0].equalsIgnoreCase("create")) {
					StatType[] values = StatType.values();
					for (int i = 0; i < values.length; i++) 
						if (values[i] != null)
							items.add(ChatColor.stripColor(values[i].translateName().replace(" ", "_")));
				}
			} else if (args.length == 3) {
				if (args[0].equalsIgnoreCase("create")) {
					TimePeriod[] values = TimePeriod.values();
					for (int i = 0; i < values.length; i++)
						items.add(ChatColor.stripColor(values[i].translateName().replace(" ", "_")));
				}
			}
		}

		if (!args[args.length - 1].trim().isEmpty()) {
			String match = args[args.length - 1].trim().toLowerCase();
			Iterator<String> it = items.iterator();
			while (it.hasNext()) {
				String name = it.next();
				if (!name.toLowerCase().startsWith(match))
					it.remove();
			}
		}
		return items;
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length == 0)
			return false;
		Player p = (Player) sender;
		NPC npc;
		if (CompatibilityManager.isPluginLoaded(CitizensCompat.class)) {
			MasterMobHunterManager masterMobHunterManager = CitizensCompat.getManager();
			npc = CitizensAPI.getDefaultNPCSelector().getSelected(sender);
			if (npc == null && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete")
					|| args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("despawn")
					|| args[0].equalsIgnoreCase("tphere") || args[0].equalsIgnoreCase("sethome"))) {
				sender.sendMessage(Messages.getString("mobhunting.commands.npc.no_npc_selected"));
				return true;
			}

			if (args.length == 1 && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete"))) {
				if (masterMobHunterManager.contains(npc.getId())) {
					masterMobHunterManager.remove(npc.getId());
				}
				npc.destroy();
				return true;

			} else if (args.length == 1 && args[0].equalsIgnoreCase("spawn")) {
				npc.spawn(npc.getStoredLocation());
				return true;

			} else if (args.length == 1 && args[0].equalsIgnoreCase("despawn")) {
				npc.despawn();
				return true;

			} else if (args.length == 1 && args[0].equalsIgnoreCase("tphere")) {
				if (masterMobHunterManager.contains(npc.getId())) {
					npc.teleport(((Player) sender).getLocation(), TeleportCause.PLUGIN);
					Block b = Misc.getTargetBlock((Player) sender, 200);
					if (b != null)
						npc.faceLocation(b.getLocation());
					// npc.getEntity().teleport((Player)sender);
				}
				return true;

			} else if (args.length == 1 && args[0].equalsIgnoreCase("sethome")) {
				if (masterMobHunterManager.contains(npc.getId())) {
					masterMobHunterManager.get(npc.getId()).setHome(npc.getEntity().getLocation());
					sender.sendMessage(Messages.getString("mobhunting.commands.npc.home_set"));
				}
				return true;

			} else if (args.length == 1 && args[0].equalsIgnoreCase("update")) {
				sender.sendMessage(Messages.getString("mobhunting.commands.npc.updating"));
				masterMobHunterManager.forceUpdate();
				return true;

			} else if (args.length == 1 && args[0].equalsIgnoreCase("select")) {
				sender.sendMessage(Messages.getString("mobhunting.commands.npc.selected", npc.getName(), npc.getId()));
				return true;

			} else if (args.length == 4 && args[0].equalsIgnoreCase("create")) {
				StatType statType = StatType.parseStat(args[1]);
				if (statType == null) {
					sender.sendMessage(ChatColor.RED
							+ Messages.getString("mobhunting.commands.npc.unknown_stattype", "stattype", args[1]));
					return true;
				}
				TimePeriod period = TimePeriod.parsePeriod(args[2]);
				if (period == null) {
					sender.sendMessage(ChatColor.RED
							+ Messages.getString("mobhunting.commands.npc.unknown_timeperiod", "period", args[2]));
					return true;
				}
				int rank = Integer.valueOf(args[3]);
				if (rank < 1 || rank > 25) {
					sender.sendMessage(ChatColor.RED
							+ Messages.getString("mobhunting.commands.npc.unknown_rank", "rank", args[3]));
					return true;
				}
				NPCRegistry registry = CitizensAPI.getNPCRegistry();
				npc = registry.createNPC(EntityType.PLAYER, "MasterMobHunter");
				npc.addTrait(MasterMobHunterTrait.class);
				masterMobHunterManager.put(npc.getId(), new MasterMobHunter(npc.getId(), statType, masterMobHunterSign, period, 0, rank));
				npc.spawn(p.getLocation());
				masterMobHunterManager.update(npc);
				sender.sendMessage(
						ChatColor.GREEN + Messages.getString("mobhunting.commands.npc.created", "npcid", npc.getId()));
				Messages.debug("Creating MasterMobHunter: id=%s,stat=%s,per=%s,rank=%s", npc.getId(),
						statType.translateName(), period, rank);
				return true;
			}

		}
		return false;
	}
}
