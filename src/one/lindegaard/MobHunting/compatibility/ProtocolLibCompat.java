package one.lindegaard.MobHunting.compatibility;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.util.Misc;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;

public class ProtocolLibCompat {

	private  Plugin mPlugin;
	private  boolean supported = false;
	private ProtocolLibHelper protocolLibHelper;
	private ConfigManager configManager;

	// https://www.spigotmc.org/resources/protocollib.1997/

	public ProtocolLibCompat(ProtocolLibHelper protocolLibHelper, ConfigManager configManager) {
		this.protocolLibHelper = protocolLibHelper;
		this.configManager = configManager;
		if (isDisabledInConfig()) {
			Bukkit.getLogger().info("[MobHunting] Compatibility with ProtocolLib is disabled in config.yml");
		} else {
			mPlugin = Bukkit.getPluginManager().getPlugin("ProtocolLib");
			if (mPlugin.getDescription().getVersion().compareTo("4.1.0") < 0 && Misc.isMC18OrNewer()) {
				ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
				console.sendMessage(ChatColor.RED + "[MobHunting] Your current version of ProtocolLib ("
						+ mPlugin.getDescription().getVersion()
						+ ") is not supported by MobHunting, please upgrade to 4.1.0 or newer.");
			} else {
				Bukkit.getLogger().info("[MobHunting] Enabling compatibility with ProtocolLib ("
						+ mPlugin.getDescription().getVersion() + ").");
				this.protocolLibHelper.enableProtocolLib();
				supported = true;
			}
		}
	}

	// **************************************************************************
	// OTHER
	// **************************************************************************

	public Plugin getProtocoloLib() {
		return mPlugin;
	}

	public  boolean isSupported() {
		return supported;
	}

	public  boolean isDisabledInConfig() {
		return configManager.disableIntegrationProtocolLib;
	}

	public  boolean isEnabledInConfig() {
		return !configManager.disableIntegrationProtocolLib;
	}

}
