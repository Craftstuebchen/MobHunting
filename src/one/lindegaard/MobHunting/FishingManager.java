package one.lindegaard.MobHunting;

import com.gmail.nossr50.datatypes.skills.SkillType;
import one.lindegaard.MobHunting.commands.HappyHourCommand;
import one.lindegaard.MobHunting.compatibility.FactionsCompat;
import one.lindegaard.MobHunting.compatibility.McMMOCompat;
import one.lindegaard.MobHunting.events.MobHuntFishingEvent;
import one.lindegaard.MobHunting.mobs.ExtendedMob;
import one.lindegaard.MobHunting.modifier.*;
import one.lindegaard.MobHunting.util.Misc;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class FishingManager implements Listener {

    private Set<IModifier> mFishingModifiers = new HashSet<IModifier>();

    private MobHunting plugin;
    private ConfigManager configManager;
    private HappyHourCommand happyHourCommand;
    private FactionsCompat factionsCompat;
    private Messages messages;

    public FishingManager(MobHunting plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.happyHourCommand=plugin.getHappyHourCommand();
        this.factionsCompat=plugin.getFactionsCompat();
        this.messages=plugin.getMessages();
        if (!configManager.disableFishingRewards) {
            registerFishingModifiers();
            Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    private void registerFishingModifiers() {
        mFishingModifiers.add(new DifficultyBonus(configManager));
        mFishingModifiers.add(new HappyHourBonus(configManager,messages,happyHourCommand));
        mFishingModifiers.add(new RankBonus(configManager));
        if (factionsCompat.isSupported())
            mFishingModifiers.add(new FactionWarZoneBonus(configManager));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void Fish(PlayerFishEvent event) {

        if (event.isCancelled()) {
            messages.debug("FishingEvent: event was cancelled");
            return;
        }

        Player player = event.getPlayer();
        if (player == null) {
            messages.debug("FishingEvent: player was null");
            return;
        }

        if (!plugin.getMobHuntingManager().isHuntEnabled(player)) {
            messages.debug("FishingEvent %s: Player doesnt have permission mobhunting.enable", player.getName());
            return;
        }

        State state = event.getState();
        Entity fish = event.getCaught();

        if (fish == null || !(fish instanceof Item))
            messages.debug("FishingEvent: State=%s", state);
        else
            messages.debug("FishingEvent: State=%s, %s caught a %s", state, player.getName(),
                    ((Item) fish).getItemStack().getData());

        switch (state) {
            case CAUGHT_FISH:
                // When a player has successfully caught a fish and is reeling it
                // in.
                // break;
            case CAUGHT_ENTITY:
                // When a player has successfully caught an entity
                if (player.getGameMode() != GameMode.SURVIVAL) {
                    messages.debug("FishingBlocked: %s is not in survival mode", player.getName());
                    messages.learn(player, messages.getString("mobhunting.learn.survival"));
                    return;
                }

                if (fish == null || !(fish instanceof Item)
                        || ((Item) fish).getItemStack().getType() != Material.RAW_FISH) {
                    messages.debug("FishingBlocked: %s only get rewards for fish", player.getName());
                    return;
                }

                Material material_under_hook = fish.getLocation().getBlock().getType();
                if (!(material_under_hook == Material.WATER || material_under_hook == Material.STATIONARY_WATER)) {
                    messages.debug("FishingBlocked: %s was fishing on %s", player.getName(), material_under_hook);
                    return;
                }

                // Calculate basic the reward
                ExtendedMob eMob = plugin.getExtendedMobManager().getExtendedMobFromEntity(fish);
                if (eMob.getMob_id() == 0) {
                    Bukkit.getLogger().warning("Unknown Mob:" + eMob.getMobName() + " from plugin " + eMob.getMobPlugin());
                    Bukkit.getLogger().warning("Please report this to developer!");
                    return;
                }
                double cash = configManager.getBaseKillPrize(fish);

                messages.debug("Basic Prize=%s for catching a %s", plugin.getRewardManager().format(cash),
                        eMob.getMobName());

                // Pay the reward to player and assister
                if ((cash >= configManager.minimumReward)
                        || (cash <= -configManager.minimumReward)) {

                    // Apply the modifiers to Basic reward
                    double multipliers = 1.0;
                    HashMap<String, Double> multiplierList = new HashMap<String, Double>();
                    ArrayList<String> modifiers = new ArrayList<String>();
                    for (IModifier mod : mFishingModifiers) {
                        if (mod.doesApply(fish, player, null, null, null)) {
                            double amt = mod.getMultiplier(fish, player, null, null, null);
                            if (amt != 1.0) {
                                messages.debug("Multiplier: %s = %s", mod.getName(), amt);
                                modifiers.add(mod.getName());
                                multiplierList.put(mod.getName(), amt);
                                multipliers *= amt;
                            }
                        }
                    }

                    // Handle MobHuntFishingEvent
                    MobHuntFishingEvent event2 = new MobHuntFishingEvent(player, fish, cash, multiplierList);
                    Bukkit.getPluginManager().callEvent(event2);
                    if (event2.isCancelled()) {
                        messages.debug("FishingBlocked %s: MobHuntFishingEvent was cancelled by another plugin",
                                player.getName());
                        return;
                    }

                    String extraString = "";

                    // Only display the multiplier if its not 1
                    if (Math.abs(multipliers - 1) > 0.05)
                        extraString += String.format("x%.1f", multipliers);

                    // Add on modifiers
                    int i = 0;
                    for (String modifier : modifiers) {
                        if (i == 0)
                            extraString += ChatColor.WHITE + " ( " + modifier;
                        else
                            extraString += ChatColor.WHITE + " * " + modifier;
                        i++;
                    }
                    if (i != 0)
                        extraString += ChatColor.WHITE + " ) ";

                    cash *= multipliers;

                    cash = Misc.ceil(cash);

                    if (cash >= configManager.minimumReward) {
                        plugin.getRewardManager().depositPlayer(player, cash);
                        messages.debug("%s got a reward (%s)", player.getName(),
                                plugin.getRewardManager().format(cash));
                    } else if (cash <= -configManager.minimumReward) {
                        plugin.getRewardManager().withdrawPlayer(player, -cash);
                        messages.debug("%s got a penalty (%s)", player.getName(),
                                plugin.getRewardManager().format(cash));
                    }

                    // Record Fishing Achievement is done using
                    // SeventhHuntAchievement.java (onFishingCompleted)

                    // Record the kill in the Database
                    messages.debug("RecordFishing: %s caught a %s (%s)", player.getName(), eMob.getMobName(),
                            eMob.getMobPlugin().name());
                    plugin.getDataStoreManager().recordKill(player, eMob, player.hasMetadata("MH:hasBonus"), cash);

                    // Handle Muted mode
                    boolean fisherman_muted = false;
                    if (plugin.getPlayerSettingsmanager().containsKey(player))
                        fisherman_muted = plugin.getPlayerSettingsmanager().getPlayerSettings(player).isMuted();

                    // Tell the player that he got the reward/penalty,
                    // unless
                    // muted
                    if (!fisherman_muted)
                        if (extraString.trim().isEmpty()) {
                            if (cash >= configManager.minimumReward) {
                                messages.playerActionBarMessage(player,
                                        ChatColor.GREEN + "" + ChatColor.ITALIC
                                                + messages.getString("mobhunting.fishcaught.reward", "prize",
                                                plugin.getRewardManager().format(cash)));
                            } else if (cash <= -configManager.minimumReward) {
                                messages.playerActionBarMessage(player,
                                        ChatColor.RED + "" + ChatColor.ITALIC
                                                + messages.getString("mobhunting.fishcaught.penalty", "prize",
                                                plugin.getRewardManager().format(cash)));
                            }

                        } else {
                            if (cash >= configManager.minimumReward) {
                                messages.debug("Message to send to ActionBar=%s", ChatColor.GREEN + "" + ChatColor.ITALIC
                                        + messages.getString("mobhunting.fishcaught.reward.bonuses", "prize",
                                        plugin.getRewardManager().format(cash), "bonuses", extraString.trim(),
                                        "multipliers", plugin.getRewardManager().format(multipliers)));
                                messages.playerActionBarMessage(player, ChatColor.GREEN + "" + ChatColor.ITALIC
                                        + messages.getString("mobhunting.fishcaught.reward.bonuses", "prize",
                                        plugin.getRewardManager().format(cash), "bonuses", extraString.trim(),
                                        "multipliers", plugin.getRewardManager().format(multipliers)));
                            } else if (cash <= -configManager.minimumReward) {
                                messages.playerActionBarMessage(player, ChatColor.RED + "" + ChatColor.ITALIC
                                        + messages.getString("mobhunting.fishcaught.penalty.bonuses", "prize",
                                        plugin.getRewardManager().format(cash), "bonuses", extraString.trim(),
                                        "multipliers", plugin.getRewardManager().format(multipliers)));
                            } else
                                messages.debug("FishingBlocked %s: Reward was less than %s", player.getName(),
                                        configManager.minimumReward);
                        }

                    // McMMO Experience rewards
                    if (McMMOCompat.isSupported() && configManager.enableMcMMOLevelRewards) {
                        double chance = plugin.getMobHuntingManager().mRand.nextDouble();
                        int level = configManager.getMcMMOLevel(fish);
                        messages.debug("If %s<%s %s will get a McMMO Level for fishing", chance,
                                configManager.getMcMMOChance(fish), player.getName());
                        if (chance < configManager.getMcMMOChance(fish)) {
                            McMMOCompat.addLevel(player, SkillType.FISHING.getName(), level);
                            messages.debug("%s was rewarded with %s McMMO level for Fishing", player.getName(), level);
                            player.sendMessage(messages.getString("mobhunting.mcmmo.fishing_level", "mcmmo_level", level));
                        }
                    }

                    String fishermanPos = player.getLocation().getBlockX() + " " + player.getLocation().getBlockY() + " "
                            + player.getLocation().getBlockZ();
                    if (configManager.isCmdGointToBeExcuted(fish)) {
                        String worldname = player.getWorld().getName();
                        String prizeCommand = configManager.getKillConsoleCmd(fish)
                                .replaceAll("\\{player\\}", player.getName()).replaceAll("\\{killer\\}", player.getName())
                                .replaceAll("\\{world\\}", worldname)
                                .replace("\\{prize\\}", plugin.getRewardManager().format(cash))
                                .replaceAll("\\{killerpos\\}", fishermanPos);
                        messages.debug("command to be run is:" + prizeCommand);
                        if (!configManager.getKillConsoleCmd(fish).equals("")) {
                            String str = prizeCommand;
                            do {
                                if (str.contains("|")) {
                                    int n = str.indexOf("|");
                                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                                            str.substring(0, n));
                                    str = str.substring(n + 1, str.length());
                                }
                            } while (str.contains("|"));
                            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), str);
                        }

                        // send a message to the player
                        if (!configManager.getKillRewardDescription(fish).equals("") && !fisherman_muted) {
                            String message = ChatColor.GREEN + "" + ChatColor.ITALIC + configManager
                                    .getKillRewardDescription(fish).replaceAll("\\{player\\}", player.getName())
                                    .replaceAll("\\{killer\\}", player.getName())
                                    .replace("\\{prize\\}", plugin.getRewardManager().format(cash))
                                    .replaceAll("\\{world\\}", worldname).replaceAll("\\{killerpos\\}", fishermanPos);

                            messages.debug("Description to be send:" + message);
                            player.sendMessage(message);
                        }
                    }
                }
                break;
            case BITE:
                // Called when there is a bite on the hook and it is ready to be
                // reeled in.
                break;
            case FAILED_ATTEMPT:
                // When a player fails to catch anything while fishing usually due
                // to poor aiming or timing
                break;
            case FISHING:
                // When a player is fishing, ie casting the line out.
                break;
            case IN_GROUND:
                // When a bobber is stuck in the ground
                messages.debug("State is IN_GROUND");
                break;
            // default:
            // break;

        }

    }

    public Set<IModifier> getFishingModifiers() {
        return mFishingModifiers;
    }

}
