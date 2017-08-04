package one.lindegaard.MobHunting.commands;

import one.lindegaard.MobHunting.HuntData;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.compatibility.ProtocolLibHelper;
import one.lindegaard.MobHunting.grinding.Area;
import one.lindegaard.MobHunting.grinding.GrindingManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CheckGrindingCommand implements ICommand {

	private GrindingManager grindingManager;
	private ProtocolLibHelper protocolLibHelper;
	private Messages messages;

	public CheckGrindingCommand(GrindingManager grindingManager, ProtocolLibHelper protocolLibHelper, Messages messages) {
		this.grindingManager = grindingManager;
		this.protocolLibHelper = protocolLibHelper;
		this.messages = messages;
	}


	@Override
	public String getName() {
		return "checkgrinding";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "isgrinding", "grinding", "checkarea" };
	}

	@Override
	public String getPermission() {
		return "mobhunting.checkgrinding";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] { ChatColor.GOLD + label };
	}

	@Override
	public String getDescription() {
		return messages.getString("mobhunting.commands.grinding.description");
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
		if (args.length != 0)
			return false;

		Location loc = ((Player) sender).getLocation();

		if (grindingManager.isWhitelisted(loc)) {
			sender.sendMessage(ChatColor.RED + messages.getString("mobhunting.commands.grinding.whitelisted"));
			Area area = grindingManager.getWhitelistArea(loc);
			protocolLibHelper.showGrindingArea((Player) sender, area, loc);
		} else if (grindingManager.isGrindingArea(loc)) {
			sender.sendMessage(ChatColor.RED + messages.getString("mobhunting.commands.grinding.blacklisted"));
			Area area = grindingManager.getGrindingArea(loc);
			protocolLibHelper.showGrindingArea((Player) sender, area, loc);
		} else {
			Area area = null;
			ArrayList<Player> players = new ArrayList<Player>();
			for (Player player : Bukkit.getOnlinePlayers()) {
				HuntData data = new HuntData(player);
				area = data.getPlayerSpecificGrindingArea(loc);
				if (area != null)
					players.add(player);
			}

			if (players.isEmpty())
				sender.sendMessage(ChatColor.GREEN + messages.getString("mobhunting.commands.grinding.not-grinding"));
			else {
				String playerList = "";

				for (Player player : players) {
					if (!playerList.isEmpty())
						playerList += ", ";

					playerList += player.getName();
				}

				sender.sendMessage(ChatColor.RED
						+ messages.getString("mobhunting.commands.grinding.player-grinding", "players", playerList));
				protocolLibHelper.showGrindingArea((Player) sender, area, loc);
			}
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
		return null;
	}

}
