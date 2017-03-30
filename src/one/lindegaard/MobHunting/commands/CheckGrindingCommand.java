package one.lindegaard.MobHunting.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import one.lindegaard.MobHunting.HuntData;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.grinding.Area;

public class CheckGrindingCommand implements ICommand {

	@Override
	public String getName() {
		return "checkgrinding";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "isgrinding", "grinding" };
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
		return Messages.getString("mobhunting.commands.grinding.description");
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
		Area area = MobHunting.getGrindingManager().getGrindingArea(loc);

		if (area != null)
			sender.sendMessage(ChatColor.RED + Messages.getString("mobhunting.commands.grinding.server-wide"));
		else {
			ArrayList<Player> players = new ArrayList<Player>();
			for (Player player : Bukkit.getOnlinePlayers()) {
				HuntData data = MobHunting.getMobHuntingManager().getHuntData(player);
				area = data.getGrindingArea(loc);
				if (area != null)
					players.add(player);
			}

			if (players.isEmpty())
				sender.sendMessage(ChatColor.GREEN + Messages.getString("mobhunting.commands.grinding.not-grinding")); //$NON-NLS-1$
			else {
				String playerList = ""; //$NON-NLS-1$

				for (Player player : players) {
					if (!playerList.isEmpty())
						playerList += ", "; //$NON-NLS-1$

					playerList += player.getName();
				}

				sender.sendMessage(ChatColor.RED
						+ Messages.getString("mobhunting.commands.grinding.player-grinding", "players", playerList)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
