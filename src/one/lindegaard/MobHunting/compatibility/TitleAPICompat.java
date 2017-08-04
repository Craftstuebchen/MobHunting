package one.lindegaard.MobHunting.compatibility;

import com.connorlinfoot.titleapi.TitleAPI;
import one.lindegaard.MobHunting.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TitleAPICompat {

    private Plugin mPlugin;
    private boolean supported = false;
    private ConfigManager configManager;

    public TitleAPICompat(ConfigManager configManager) {
        this.configManager = configManager;
        if (isDisabledInConfig()) {
            Bukkit.getLogger().info("[MobHunting] Compatibility with TitelAPI is disabled in config.yml");
        } else {
            mPlugin = Bukkit.getPluginManager().getPlugin("TitleAPI");

            Bukkit.getLogger().info("[MobHunting] Enabling compatibility with TitleAPI ("
                    + getTitleAPI().getDescription().getVersion() + ")");
            supported = true;
        }
    }

    // **************************************************************************
    // OTHER
    // **************************************************************************

    public Plugin getTitleAPI() {
        return mPlugin;
    }

    public boolean isSupported() {
        return supported;
    }

    public boolean isDisabledInConfig() {
        return configManager.disableIntegrationTitleAPI;
    }

    public boolean isEnabledInConfig() {
        return !configManager.disableIntegrationTitleAPI;
    }

    public void sendTitles(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (supported)
            TitleAPI.sendTitle(player, fadeIn, stay, fadeOut, title, subtitle);
    }

    public void sendTabTitle(Player player, String header, String footer) {
        if (supported)
            TitleAPI.sendTabTitle(player, header, footer);

    }
}
