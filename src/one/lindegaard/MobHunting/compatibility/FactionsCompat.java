package one.lindegaard.MobHunting.compatibility;

import com.massivecraft.factions.entity.*;
import com.massivecraft.massivecore.ps.PS;
import one.lindegaard.MobHunting.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class FactionsCompat {

    private Plugin mPlugin;
    private boolean supported = false;

    // https://www.massivecraft.com/factions-develop

    private ConfigManager configManager;


    public FactionsCompat(ConfigManager configManager) {
        this.configManager = configManager;
        if (isDisabledInConfig()) {
            Bukkit.getLogger().info("[MobHunting] Compatibility with Factions in config.yml");
        } else {
            mPlugin = Bukkit.getPluginManager().getPlugin("Factions");
            if (mPlugin.getDescription().getVersion().compareTo("1.6.9.6") < 0) {
                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                console.sendMessage(ChatColor.RED + "[MobHunting] Your version of Factions ("
                        + mPlugin.getDescription().getVersion() + ") is not supported by MobHunting, please upgrade.");
            } else {
                try {
                    @SuppressWarnings({"rawtypes", "unused"})
                    Class cls = Class.forName("com.massivecraft.factions.entity.BoardColl");
                    Bukkit.getLogger().info("[MobHunting] Enabling compatibility with Factions ("
                            + mPlugin.getDescription().getVersion() + ").");
                    supported = true;
                } catch (ClassNotFoundException e) {
                    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                    console.sendMessage(ChatColor.RED + "[MobHunting] Your version of Factions ("
                            + mPlugin.getDescription().getVersion()
                            + ") is not complatible with this version of MobHunting, please upgrade.");
                }
            }
        }
    }

    // **************************************************************************
    // OTHER
    // **************************************************************************

    public Plugin getPlugin() {
        return mPlugin;
    }

    public boolean isSupported() {
        return supported;
    }

    public boolean isDisabledInConfig() {
        return configManager.disableIntegrationFactions;
    }

    public boolean isEnabledInConfig() {
        return !configManager.disableIntegrationFactions;
    }

    // PERMANENT: A permanent faction will never be deleted. (no)
    // PEACEFUL: Allways in truce with other factions (no)
    // INFPOWER: This flag gives the faction infinite power. (no)
    // POWERLOSS: Is power lost on death in this territory? (yes)
    // PVP: Can you PVP in territory? (yes)
    // FRIENDLYFIRE: Can friends hurt eachother here? (no)
    // MONSTERS: Can monsters spawn in this territory? (yes)
    // EXPLOSIONS: Can explosions occur in this territory? (yes)
    // FIRESPREAD: Can fire spread in territory? (yes)
    // ENDERGRIEF: Can endermen grief in this territory? (no

    public boolean isPVPAllowed(Player player) {
        if (supported) {
            MPlayer mplayer = MPlayer.get(player);
            Faction faction = mplayer.getFaction();
            return faction.getFlag(MFlag.ID_FRIENDLYFIRE);
        }
        return false;
    }

    public boolean isMonstersAllowedToSpawn(Player player) {
        if (supported) {
            MPlayer mplayer = MPlayer.get(player);
            Faction faction = mplayer.getFaction();
            return faction.getFlag(MFlag.ID_MONSTERS);
        }
        return false;
    }

    public boolean isInSafeZone(Player player) {
        if (supported) {
            Faction faction = BoardColl.get().getFactionAt(PS.valueOf(player.getLocation()));
            return FactionColl.get().getSafezone().equals(faction);
        }
        return false;
    }

    public boolean isInWilderness(Player player) {
        if (supported) {
            Faction faction = BoardColl.get().getFactionAt(PS.valueOf(player.getLocation()));
            return FactionColl.get().getNone().equals(faction);
        }
        return false;
    }

    public boolean isInWarZone(Player player) {
        if (supported) {
            Faction faction = BoardColl.get().getFactionAt(PS.valueOf(player.getLocation()));
            return FactionColl.get().getWarzone().equals(faction);
        }
        return false;
    }
}
