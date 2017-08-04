package one.lindegaard.MobHunting.compatibility;

import mc.alk.arena.events.players.ArenaPlayerJoinEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.objects.ArenaPlayer;
import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BattleArenaCompat implements Listener {

    private Plugin mPlugin;
    private List<UUID> playersPlayingBattleArena = new ArrayList<UUID>();
    private boolean supported = false;

    private ConfigManager configManager;
    private Messages messages;

    public BattleArenaCompat(ConfigManager configManager, Messages messages) {
        this.configManager = configManager;
        this.messages = messages;
        if (isDisabledInConfig()) {
            Bukkit.getConsoleSender()
                    .sendMessage("[MobHunting] Compatibility with BattleArena is disabled in config.yml");
        } else {
            mPlugin = Bukkit.getPluginManager().getPlugin("BattleArena");

            Bukkit.getPluginManager().registerEvents(this, MobHunting.getInstance());

            Bukkit.getConsoleSender().sendMessage("[MobHunting] Enabling compatibility with BattleArena ("
                    + getBattleArena().getDescription().getVersion() + ")");
            supported = true;
        }
    }

    // **************************************************************************
    // OTHER
    // **************************************************************************

    public Plugin getBattleArena() {
        return mPlugin;
    }

    public boolean isSupported() {
        return supported;
    }

    public boolean isDisabledInConfig() {
        return configManager.disableIntegrationBattleArena;
    }

    public boolean isEnabledInConfig() {
        return !configManager.disableIntegrationBattleArena;
    }

    /**
     * Determine if the player is currently playing BattleArena
     *
     * @param player
     * @return Returns true when the player is in game.
     */
    public boolean isPlayingBattleArena(Player player) {
        if (isSupported())
            return playersPlayingBattleArena.contains(player.getUniqueId());
        return false;
    }

    /**
     * Add the player to the list of active BattleArena players.
     *
     * @param arenaPlayer
     */
    public void startPlayingBattleArena(ArenaPlayer arenaPlayer) {
        playersPlayingBattleArena.add(arenaPlayer.getID());
    }

    /**
     * Remove the player from list of active BattleArena players
     *
     * @param arenaPlayer
     */
    public void stopPlayingBattleArena(ArenaPlayer arenaPlayer) {
        if (!playersPlayingBattleArena.remove(arenaPlayer.getID())) {
            messages.debug("Player: %s is not a the BattleArena", arenaPlayer.getName());
        }
    }

    // **************************************************************************
    // EVENTS
    // **************************************************************************
    @EventHandler(priority = EventPriority.NORMAL)
    private void onArenaPlayerJoinEvent(ArenaPlayerJoinEvent event) {
        messages.debug("BattleArenaCompat.StartEvent s%", event.getEventName());
        startPlayingBattleArena(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onArenaPlayerLeaveEvent(ArenaPlayerLeaveEvent event) {
        messages.debug("BattleArenaCompat.StartEvent %s", event.getEventName());
        stopPlayingBattleArena(event.getPlayer());
    }

}
