package one.lindegaard.MobHunting.compatibility;

import one.lindegaard.MobHunting.ConfigManager;
import org.bukkit.Bukkit;
import org.gestern.gringotts.Gringotts;

public class GringottsCompat {

    // http://dev.bukkit.org/bukkit-plugins/gringotts/
    // Source code: https://github.com/MinecraftWars/Gringotts

    private boolean supported = false;
    private Gringotts mPlugin;

    private ConfigManager configManager;

    public GringottsCompat(ConfigManager configManager) {
        this.configManager = configManager;
        if (isDisabledInConfig()) {
            Bukkit.getLogger().info("[MobHunting] Compatibility with Gringotts is disabled in config.yml");
        } else {
            mPlugin = (Gringotts) Bukkit.getPluginManager().getPlugin("Gringotts");

            Bukkit.getLogger().info(
                    "[MobHunting] Enabling Compatibility with Gringotts (" + getGringotts().getDescription().getVersion() + ")");
            supported = true;
        }
    }

    // **************************************************************************
    // OTHER FUNCTIONS
    // **************************************************************************
    public Gringotts getGringotts() {
        return mPlugin;
    }

    public boolean isSupported() {
        return supported;
    }

    public boolean isDisabledInConfig() {
        return configManager.disableIntegrationGringotts;
    }

    public boolean isEnabledInConfig() {
        return !configManager.disableIntegrationGringotts;
    }

}
