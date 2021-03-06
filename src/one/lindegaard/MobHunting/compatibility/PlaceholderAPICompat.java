package one.lindegaard.MobHunting.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;

import me.clip.placeholderapi.PlaceholderAPI;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.util.Misc;

public class PlaceholderAPICompat {

	private static Plugin mPlugin;
	private static boolean supported = false;

	// https://www.spigotmc.org/wiki/hooking-into-placeholderapi/

	public PlaceholderAPICompat() {
		if (isDisabledInConfig()) {
			Bukkit.getLogger().info("[MobHunting] Compatibility with PlaceholderAPI is disabled in config.yml");
		} else {
			mPlugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
			if (mPlugin.getDescription().getVersion().compareTo("2.0.6") >= 0 && Misc.isMC18OrNewer()) {
				Bukkit.getLogger().info("[MobHunting] Enabling compatibility with PlaceholderAPI ("
						+ mPlugin.getDescription().getVersion() + ").");
				new PlaceholderHook(MobHunting.getInstance()).hook();
				supported = true;
			} else {
				ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
				console.sendMessage(ChatColor.RED + "[MobHunting] Your current version of PlaceholderAPI ("
						+ mPlugin.getDescription().getVersion()
						+ ") is not supported by MobHunting, please upgrade to 2.0.6 or newer.");
			}
		}
	}

	// **************************************************************************
	// OTHER
	// **************************************************************************

	public PlaceholderAPI getPlaceholderAPI() {
		return (PlaceholderAPI) mPlugin;
	}

	public static boolean isSupported() {
		return supported;
	}

	public static boolean isDisabledInConfig() {
		return MobHunting.getConfigManager().disableIntegrationPlaceholderAPI;
	}

	public static boolean isEnabledInConfig() {
		return !MobHunting.getConfigManager().disableIntegrationPlaceholderAPI;
	}

}
