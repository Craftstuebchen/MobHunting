package one.lindegaard.MobHunting.commands;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.compatibility.ProtocolLibHelper;
import one.lindegaard.MobHunting.grinding.Area;
import one.lindegaard.MobHunting.grinding.GrindingManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BlacklistAreaCommand implements ICommand {

	private Messages messages;
	private ConfigManager configManager;
	private GrindingManager grindingManager;
	private ProtocolLibHelper protocolLibHelper;

	public BlacklistAreaCommand(Messages messages, ConfigManager configManager, GrindingManager grindingManager, ProtocolLibHelper protocolLibHelper) {
		this.messages = messages;
        this.configManager = configManager;
        this.grindingManager = grindingManager;
        this.protocolLibHelper = protocolLibHelper;
    }

	@Override
	public String getName() {
		return "blacklistarea";
	}

	@Override
	public String[] getAliases() {
		return null;
	}

	@Override
	public String getPermission() {
		return "mobhunting.blacklist";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] { ChatColor.GOLD + label + ChatColor.GREEN + " [add|remove]" + ChatColor.WHITE
				+ " - to blacklist an area." };
	}

	@Override
	public String getDescription() {
		return messages.getString("mobhunting.commands.blacklistarea.description");
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
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		Location loc = ((Player) sender).getLocation();

		if (args.length == 0) {
			if (grindingManager.isGrindingArea(loc)) {
				sender.sendMessage(
						ChatColor.GREEN + messages.getString("mobhunting.commands.blacklistarea.isblacklisted"));
				Area area = grindingManager.getGrindingArea(loc);
				protocolLibHelper.showGrindingArea((Player) sender, area, loc);
			} else
				sender.sendMessage(
						ChatColor.RED + messages.getString("mobhunting.commands.blacklistarea.notblacklisted"));
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("remove")) {
				grindingManager.unBlacklistArea(loc);
				sender.sendMessage(
						ChatColor.GREEN + messages.getString("mobhunting.commands.blacklistarea.remove.done"));
			} else if (args[0].equalsIgnoreCase("add")) {
				Area area = new Area(loc, configManager.grindingDetectionRange, 0);
				grindingManager.blacklistArea(area);
				sender.sendMessage(ChatColor.GREEN + messages.getString("mobhunting.commands.blacklistarea.done"));
                protocolLibHelper.showGrindingArea((Player) sender, area, loc);
			} else
				return false;
		} else
			return false;

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
		ArrayList<String> items = new ArrayList<String>();
		if (args.length == 1) {
			if (items.isEmpty()) {
				items.add("add");
				items.add("remove");
				items.add("");
			}
		} 
		
		if (!args[args.length - 1].trim().isEmpty()) {
			String match = args[args.length - 1].trim().toLowerCase();
			items.removeIf(name -> !name.toLowerCase().startsWith(match));
		}
		return items;
	}

}
