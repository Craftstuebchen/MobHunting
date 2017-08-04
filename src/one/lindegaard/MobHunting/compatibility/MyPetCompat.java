package one.lindegaard.MobHunting.compatibility;

import de.Keyle.MyPet.MyPetPlugin;
import de.Keyle.MyPet.api.entity.MyPetBukkitEntity;
import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.MobHuntingManager;
import one.lindegaard.MobHunting.achievements.AchievementManager;
import one.lindegaard.MobHunting.mobs.ExtendedMobManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class MyPetCompat implements Listener {
    private boolean supported = false;
    private MyPetPlugin mPlugin;

    private ConfigManager configManager;
    private MobHuntingManager mobHuntingManager;
    private AchievementManager achievementManager;
    private ExtendedMobManager extendedMobManager;

    public MyPetCompat(ConfigManager configManager, MobHuntingManager mobHuntingManager,
                       AchievementManager achievementManager, ExtendedMobManager extendedMobManager) {
        this.configManager = configManager;
        this.mobHuntingManager = mobHuntingManager;
        this.achievementManager = achievementManager;
        this.extendedMobManager = extendedMobManager;

        if (configManager.disableIntegrationMyPet) {
            Bukkit.getLogger().info("[MobHunting] Compatibility with MyPet is disabled in config.yml");
        } else {
            mPlugin = (MyPetPlugin) Bukkit.getPluginManager().getPlugin("MyPet");
            Bukkit.getPluginManager().registerEvents(this, MobHunting.getInstance());
            Bukkit.getLogger().info("[MobHunting] Enabling compatibility with MyPet ("
                    + getMyPetPlugin().getDescription().getVersion() + ")");
            supported = true;
        }
    }

    // **************************************************************************
    // OTHER FUNCTIONS
    // **************************************************************************

    public boolean isSupported() {
        return supported;
    }

    public MyPetPlugin getMyPetPlugin() {
        return mPlugin;
    }

    public boolean isMyPet(Entity entity) {
        if (isSupported())
            return entity instanceof MyPetBukkitEntity;
        return false;
    }

    public boolean isEnabledInConfig() {
        return !configManager.disableIntegrationMyPet;
    }

    public boolean isKilledByMyPet(Entity entity) {
        if (isSupported() && (entity.getLastDamageCause() instanceof EntityDamageByEntityEvent)) {
            EntityDamageByEntityEvent dmg = (EntityDamageByEntityEvent) entity.getLastDamageCause();
            if (dmg != null && (dmg.getDamager() instanceof MyPetBukkitEntity))
                return true;
        }
        return false;
    }

    public MyPetBukkitEntity getMyPet(Entity entity) {
        EntityDamageByEntityEvent dmg = (EntityDamageByEntityEvent) entity.getLastDamageCause();

        if (dmg == null || !(dmg.getDamager() instanceof MyPetBukkitEntity))
            return null;

        return (MyPetBukkitEntity) dmg.getDamager();
    }

    public Player getMyPetOwner(Entity entity) {

        if (!(entity.getLastDamageCause() instanceof EntityDamageByEntityEvent))
            return null;

        EntityDamageByEntityEvent dmg = (EntityDamageByEntityEvent) entity.getLastDamageCause();

        if (dmg == null || !(dmg.getDamager() instanceof MyPetBukkitEntity))
            return null;

        MyPetBukkitEntity killer = (MyPetBukkitEntity) dmg.getDamager();

        if (killer.getOwner() == null)
            return null;

        return killer.getOwner().getPlayer();
    }

    // **************************************************************************
    // EVENTS
    // **************************************************************************
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onMyPetKillMob(EntityDeathEvent event) {
        if (!mobHuntingManager.isHuntEnabledInWorld(event.getEntity().getWorld())
                || !(event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent))
            return;

        EntityDamageByEntityEvent dmg = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
        if (dmg == null || !(dmg.getDamager() instanceof MyPetBukkitEntity))
            return;

        MyPetBukkitEntity killer = (MyPetBukkitEntity) dmg.getDamager();
        if (killer.getOwner() != null) {
            Player owner = killer.getOwner().getPlayer();
            if (owner != null && mobHuntingManager.isHuntEnabled(owner))
                achievementManager.awardAchievementProgress("fangmaster", owner,
                        extendedMobManager.getExtendedMobFromEntity(event.getEntity()), 1);
        }
    }
}
