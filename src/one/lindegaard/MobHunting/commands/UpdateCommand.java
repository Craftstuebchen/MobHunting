package one.lindegaard.MobHunting.commands;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.update.UpdateStatus;
import one.lindegaard.MobHunting.update.Updater;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class UpdateCommand implements ICommand {
	private Messages messages;

	public UpdateCommand(Messages messages) {
		this.messages = messages;
	}

	@Override
	public String getName() {
		return "update";
	}

	@Override
	public String[] getAliases() {
		return null;
	}

	@Override
	public String getPermission() {
		return "mobhunting.update";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] { ChatColor.GOLD + label + ChatColor.WHITE + " - to download and update the plugin." };
	}

	@Override
	public String getDescription() {
		return messages.getString("mobhunting.commands.update.description");
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
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		if (Updater.getUpdateAvailable() == UpdateStatus.AVAILABLE) {
			if (Updater.downloadAndUpdateJar()) {
				sender.sendMessage(ChatColor.GREEN + messages.getString("mobhunting.commands.update.complete"));
			} else {
				sender.sendMessage(ChatColor.GREEN + messages.getString("mobhunting.commands.update.could-not-update"));
			}
		} else if (Updater.getUpdateAvailable() == UpdateStatus.RESTART_NEEDED) {
			sender.sendMessage(ChatColor.GREEN + messages.getString("mobhunting.commands.update.complete"));
		} else {
			Updater.pluginUpdateCheck(sender, true, false);
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
		return null;
	}

}
