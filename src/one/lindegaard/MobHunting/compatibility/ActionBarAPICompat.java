package one.lindegaard.MobHunting.compatibility;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import one.lindegaard.MobHunting.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ActionBarAPICompat {

    private ActionBarAPI mPlugin;
    private boolean supported = false;
    private ConfigManager configManager;

    // https://www.spigotmc.org/resources/actionbarapi-1-8-1-9-1-10.1315/

    public ActionBarAPICompat(ConfigManager configManager) {
        this.configManager = configManager;
        if (isDisabledInConfig()) {
            Bukkit.getConsoleSender()
                    .sendMessage("[MobHunting] Compatibility with ActionBarAPI is disabled in config.yml");
        } else {
            mPlugin = (ActionBarAPI) Bukkit.getPluginManager().getPlugin("ActionBarAPI");

            Bukkit.getConsoleSender().sendMessage("[MobHunting] Enabling compatibility with ActionBarAPI ("
                    + getActionBarAPI().getDescription().getVersion() + ")");
            supported = true;
        }
    }

    // **************************************************************************
    // OTHER
    // **************************************************************************

    public ActionBarAPI getActionBarAPI() {
        return mPlugin;
    }

    public boolean isSupported() {
        return supported;
    }

    public boolean isDisabledInConfig() {
        return configManager.disableIntegrationActionBarAPI;
    }

    public boolean isEnabledInConfig() {
        return !configManager.disableIntegrationActionBarAPI;
    }

    public void setMessage(Player player, String text) {
        if (supported) {

            ActionBarAPI.sendActionBar(player, text);

            // ActionBarAPI.sendActionBar(player,"Action Bar Message",
            // duration);
        }
    }

}
