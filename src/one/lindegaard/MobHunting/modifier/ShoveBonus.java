package one.lindegaard.MobHunting.modifier;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.DamageInformation;
import one.lindegaard.MobHunting.HuntData;
import one.lindegaard.MobHunting.Messages;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class ShoveBonus extends IModifier {



    public ShoveBonus(ConfigManager configManager, Messages messages) {
        super(configManager,messages);
    }

    @Override
    public String getName() {
        return ChatColor.AQUA + messages.getString("bonus.ashove.name"); //$NON-NLS-1$
    }

    @Override
    public double getMultiplier(Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo,
                                EntityDamageByEntityEvent lastDamageCause) {
        return configManager.bonusSendFalling;
    }

    @Override
    public boolean doesApply(Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo,
                             EntityDamageByEntityEvent lastDamageCause) {
        if (extraInfo.getAttacker() != killer)
            return false;

        if (deadEntity.getLastDamageCause() != null)
            return deadEntity.getLastDamageCause().getCause() == DamageCause.FALL;
        return false;
    }

}
