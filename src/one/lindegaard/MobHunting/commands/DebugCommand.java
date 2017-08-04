package one.lindegaard.MobHunting.commands;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class DebugCommand implements ICommand {
	private ConfigManager configManager;
	private Messages messages;

	public DebugCommand(ConfigManager configManager, Messages messages) {
		this.configManager = configManager;
		this.messages = messages;
	}

	// Used case
	// /mh debug - args.length = 0 || arg[0]=""

	@Override
	public String getName() {
		return "debug";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "debugmode" };
	}

	@Override
	public String getPermission() {
		return "mobhunting.debug";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] { ChatColor.GOLD + label + ChatColor.WHITE + " - to enable/disable debugmode." };
	}

	@Override
	public String getDescription() {
		return messages.getString("mobhunting.commands.debug.description");
	}

	@Override
	public boolean canBeConsole() {
		return true;
	}

	@Override
	public boolean canBeCommandBlock() {
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length == 0) {
			toggledebugMode(sender);
			return true;
		}
		return false;
	}

	private void toggledebugMode(CommandSender sender) {
		boolean debug = configManager.killDebug;
		if (debug) {
			configManager.killDebug = false;
			sender.sendMessage("[MobHunting] " + messages.getString("mobhunting.commands.debug.disabled"));
			configManager.saveConfig();
		} else {
			configManager.killDebug = true;
			sender.sendMessage("[MobHunting] " + messages.getString("mobhunting.commands.debug.enabled"));
			configManager.saveConfig();
		}

	}

}
