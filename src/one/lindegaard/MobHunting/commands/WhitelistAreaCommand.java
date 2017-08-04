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

public class WhitelistAreaCommand implements ICommand {

	private Messages messages;
	private GrindingManager grindingManager;
	private ConfigManager configManager;
	private ProtocolLibHelper protocolLibHelper;

	public WhitelistAreaCommand(Messages messages, GrindingManager grindingManager, ConfigManager configManager, ProtocolLibHelper protocolLibHelper) {
		this.messages = messages;
        this.grindingManager = grindingManager;
        this.configManager = configManager;
        this.protocolLibHelper = protocolLibHelper;
    }

	@Override
	public String getName() {
		return "whitelistarea";
	}

	@Override
	public String[] getAliases() {
		return null;
	}

	@Override
	public String getPermission() {
		return "mobhunting.whitelist";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] { ChatColor.GOLD + label + ChatColor.GREEN + " [add|remove]" + ChatColor.WHITE
				+ " - to whitelist an area." };
	}

	@Override
	public String getDescription() {
		return messages.getString("mobhunting.commands.whitelistarea.description");
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
			if (grindingManager.isWhitelisted(loc)) {
				sender.sendMessage(
						ChatColor.GREEN + messages.getString("mobhunting.commands.whitelistarea.iswhitelisted"));
				Area area = grindingManager.getWhitelistArea(loc);
                protocolLibHelper.showGrindingArea((Player) sender, area, loc);
			} else
				sender.sendMessage(
						ChatColor.RED + messages.getString("mobhunting.commands.whitelistarea.notwhitelisted"));
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("remove")) {
                grindingManager.unWhitelistArea(loc);
				sender.sendMessage(
						ChatColor.GREEN + messages.getString("mobhunting.commands.whitelistarea.remove.done"));
			} else if (args[0].equalsIgnoreCase("add")) {
				Area area = new Area(loc, configManager.grindingDetectionRange, 0);
                grindingManager.whitelistArea(area);
				sender.sendMessage(ChatColor.GREEN + messages.getString("mobhunting.commands.whitelistarea.done"));
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
