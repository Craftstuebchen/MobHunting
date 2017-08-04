package one.lindegaard.MobHunting.commands;

import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.PlayerSettingsManager;
import one.lindegaard.MobHunting.storage.DataStoreManager;
import one.lindegaard.MobHunting.storage.PlayerSettings;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class MuteCommand implements ICommand {


	private DataStoreManager dataStoreManager;
	private PlayerSettingsManager playerSettingsManager;
	private Messages messages;

	public MuteCommand(MobHunting mobHunting, Messages messages) {
		this.dataStoreManager=mobHunting.getDataStoreManager();
		this.playerSettingsManager =mobHunting.getPlayerSettingsmanager();
		this.messages = messages;
	}

	// Used case
	// /mh mute - No args, args.length = 0 || arg[0]=""

	@Override
	public String getName() {
		return "mute";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "silent", "notify" };
	}

	@Override
	public String getPermission() {
		return "mobhunting.mute";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] { ChatColor.GOLD + label + ChatColor.WHITE + " - to mute/unmute.",
				ChatColor.GOLD + label + ChatColor.GREEN + " <playername>" + ChatColor.WHITE
						+ " - to mute/unmute a the notifications for a specific player." };
	}

	@Override
	public String getDescription() {
		return messages.getString("mobhunting.commands.mute.description");
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
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {

		if (args.length == 0) {
			togglePlayerMuteMode((Player) sender);
			return true;
		} else if (args.length == 1) {
			MobHunting.getInstance();
			Player player = (Player) dataStoreManager.getPlayerByName(args[0]);
			if (player != null) {
				if (sender.hasPermission("mobhunting.mute.other") || sender instanceof ConsoleCommandSender) {
					togglePlayerMuteMode(player);
				} else {
					sender.sendMessage(
							ChatColor.RED + "You dont have permission " + ChatColor.AQUA + "'mobhunting.mute.other'");
				}
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "Player " + args[0] + " is not online.");
				return false;
			}
		}
		return false;
	}

	private void togglePlayerMuteMode(Player player) {
		boolean lm = playerSettingsManager.getPlayerSettings(player).isLearningMode();
		if (playerSettingsManager.getPlayerSettings(player).isMuted()) {
			dataStoreManager.updatePlayerSettings(player, lm, false);
			playerSettingsManager.setPlayerSettings(player, new PlayerSettings(player, lm, false));
			player.sendMessage(messages.getString("mobhunting.commands.mute.unmuted", "player", player.getName()));
		} else {
			dataStoreManager.updatePlayerSettings(player, lm, true);
			playerSettingsManager.setPlayerSettings(player, new PlayerSettings(player, lm, true));
			player.sendMessage(messages.getString("mobhunting.commands.mute.muted", "player", player.getName()));
		}
	}
}
