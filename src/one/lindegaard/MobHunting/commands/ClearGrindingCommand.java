package one.lindegaard.MobHunting.commands;

import one.lindegaard.MobHunting.HuntData;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ClearGrindingCommand implements ICommand {

	private Messages messages;
	private MobHunting mobHunting;

	public ClearGrindingCommand(Messages messages, MobHunting mobHunting) {
		this.messages = messages;
		this.mobHunting = mobHunting;
	}

	@Override
	public String getName() {
		return "cleargrinding";
	}

	@Override
	public String[] getAliases() {
		return null;
	}

	@Override
	public String getPermission() {
		return "mobhunting.cleargrinding";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] { ChatColor.GOLD + label };
	}

	@Override
	public String getDescription() {
		return messages.getString("mobhunting.commands.cleargrinding.description");
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
		mobHunting.getGrindingManager().clearGrindingArea(loc);

		for (Player player : Bukkit.getOnlinePlayers()) {
			HuntData data = new HuntData(player);
			data.clearGrindingArea(loc);
		}

		sender.sendMessage(ChatColor.GREEN + messages.getString("mobhunting.commands.cleargrinding.done"));

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
		return Collections.emptyList();
	}

}
