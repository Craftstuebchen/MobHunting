package one.lindegaard.MobHunting.rewards;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.compatibility.GringottsCompat;
import one.lindegaard.MobHunting.compatibility.ProtocolLibCompat;
import one.lindegaard.MobHunting.compatibility.ProtocolLibHelper;
import one.lindegaard.MobHunting.mobs.MinecraftMob;
import one.lindegaard.MobHunting.util.Misc;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.gestern.gringotts.Configuration;
import org.gestern.gringotts.currency.Denomination;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class RewardManager implements Listener {

    // public static final String MH_MONEY = "MH:Money";
    public static final String MH_REWARD_DATA = "MH:HiddenRewardData";
    // Unique random generated UUID for "Bag of gold" rewards
    public static final String MH_REWARD_BAG_OF_GOLD_UUID = "b3f74fad-429f-4801-9e31-b8879cbae96f";
    // Unique random generated UUID for MobHead/Playerhead rewards
    public static final String MH_REWARD_KILLED_UUID = "2351844f-f400-4fa4-9642-35169c5b048a";
    // Unique random generated UUID for ITEM rewards
    public static final String MH_REWARD_ITEM_UUID = "3ffe9c3b-0445-4c35-a952-c2aaf5aeac76";
    // Unique random generated UUID for KILLER head rewards
    public static final String MH_REWARD_KILLER_UUID = "d81f1076-c91c-44c0-98c3-02a2ee88aa97";

    private File file = new File(MobHunting.getInstance().getDataFolder(), "rewards.yml");
    private YamlConfiguration config = new YamlConfiguration();

    private Economy mEconomy;

    private HashMap<Integer, Double> droppedMoney = new HashMap<>();
    private HashMap<UUID, Reward> placedMoney_Reward = new HashMap<>();
    private HashMap<UUID, Location> placedMoney_Location = new HashMap<>();


    private CustomItems customItems;


    private PickupRewards pickupRewards;
    private ConfigManager configManager;
    private Messages messages;

    private MobHunting plugin;
    private GringottsCompat gringottsCompat;

    public RewardManager(MobHunting instance) {
        this.plugin = instance;


        ProtocolLibHelper protocolLibHelper = plugin.getProtocolLibHelper();
        ProtocolLibCompat protocolLibCompat = plugin.getmProtocolLibCompat();
        this.gringottsCompat=instance.getGringottsCompat();
        RewardManager rewardManager = plugin.getRewardManager();
        this.configManager = plugin.getConfigManager();

        pickupRewards = new PickupRewards(protocolLibCompat, protocolLibHelper, rewardManager,plugin.getConfigManager(), messages);
        this.messages=instance.getMessages();



        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (economyProvider == null) {
            Bukkit.getLogger().severe(messages.getString(instance.getName().toLowerCase() + ".hook.econ"));
            Bukkit.getPluginManager().disablePlugin(instance);
            return;
        }
        mEconomy = economyProvider.getProvider();



        this.customItems = plugin.getCustomItems();
        this.customItems.setRewardManager(this);

        Bukkit.getPluginManager().registerEvents(new RewardListeners(rewardManager, protocolLibCompat, protocolLibHelper, customItems, configManager, messages), instance);
        if (Misc.isMC18OrNewer())
            Bukkit.getPluginManager().registerEvents(new MoneyMergeEventListener(rewardManager, configManager, messages), MobHunting.getInstance());

        if (Misc.isMC112OrNewer())
            Bukkit.getPluginManager().registerEvents(new EntityPickupItemEventListener(pickupRewards), MobHunting.getInstance());
        else
            Bukkit.getPluginManager().registerEvents(new PlayerPickupItemEventListener(pickupRewards), MobHunting.getInstance());

        loadAllStoredRewards();
    }

    public Economy getEconomy() {
        return mEconomy;
    }

    public HashMap<Integer, Double> getDroppedMoney() {
        return droppedMoney;
    }

    public HashMap<UUID, Reward> getLocations() {
        return placedMoney_Reward;
    }

    public HashMap<UUID, Location> getReward() {
        return placedMoney_Location;
    }

    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double amount) {
        EconomyResponse result = mEconomy.withdrawPlayer(offlinePlayer, amount);
        if (!result.transactionSuccess() && offlinePlayer.isOnline())
            ((Player) offlinePlayer).sendMessage(ChatColor.RED + "Unable to remove money: " + result.errorMessage);
        return result;
    }

    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double amount) {
        EconomyResponse result = mEconomy.depositPlayer(offlinePlayer, amount);
        if (!result.transactionSuccess() && offlinePlayer.isOnline())
            ((Player) offlinePlayer).sendMessage(ChatColor.RED + "Unable to add money: " + result.errorMessage);
        return result;
    }

    public String format(double amount) {
        return mEconomy.format(Misc.round(amount));
    }

    public double getBalance(OfflinePlayer offlinePlayer) {
        return mEconomy.getBalance(offlinePlayer);
    }

    public boolean has(OfflinePlayer offlinePlayer, double amount) {
        return mEconomy.has(offlinePlayer, amount);
    }

    public void dropMoneyOnGround(Player player, Entity killedEntity, Location location, double money) {
        Item item = null;
        money = Misc.ceil(money);
        if (gringottsCompat.isSupported()) {
            List<Denomination> denoms = Configuration.CONF.currency.denominations();
            int unit = Configuration.CONF.currency.unit;
            double rest = money;
            for (Denomination d : denoms) {
                ItemStack is = new ItemStack(d.key.type.getType(), 1);
                while (rest >= (d.value / unit)) {
                    item = location.getWorld().dropItem(location, is);
                    rest = rest - (d.value / unit);
                }
            }
        } else {
            ItemStack is;
            UUID uuid = null;
            if (plugin.getConfigManager().dropMoneyOnGroundItemtype.equalsIgnoreCase("KILLED")) {
                MinecraftMob mob = MinecraftMob.getMinecraftMobType(killedEntity);
                uuid = UUID.fromString(MH_REWARD_KILLED_UUID);
                if (mob != null)
                    is = mob.getCustomHead(mob.getFriendlyName(), 1, money);
                else // https://mineskin.org/6875
                    is = customItems.getCustomtexture(uuid,
                            plugin.getConfigManager().dropMoneyOnGroundSkullRewardName,
                            "eyJ0aW1lc3RhbXAiOjE0ODU5MTIwNjk3OTgsInByb2ZpbGVJZCI6IjdkYTJhYjNhOTNjYTQ4ZWU4MzA0OGFmYzNiODBlNjhlIiwicHJvZmlsZU5hbWUiOiJHb2xkYXBmZWwiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzM5NmNlMTNmZjYxNTVmZGYzMjM1ZDhkMjIxNzRjNWRlNGJmNTUxMmYxYWRlZGExYWZhM2ZjMjgxODBmM2Y3In19fQ==",
                            "m8u2ChI43ySVica7pcY0CsCuMCGgAdN7c9f/ZOxDZsPzJY8eiDrwxLIh6oPY1rvE1ja/rmftPSmdnbeHYrzLQ18QBzehFp8ZVegPsd9iNHc4FuD7nr1is2FD8M8AWAZOViiwlUKnfd8avb3SKfvFmhmVhQtE+atJYQrXhJwiqR4S+KTccA6pjIESM3AWlbCOmykg31ey7MQWB4YgtRp8NyFD3HNTLZ8alcEXBuG3t58wYBEME1UaOFah45tHuV1FW+iGBHHFWLu1UsAbg0Uw87Pp+KSTUGrhdwSc/55czILulI8IUnUfxmkaThRjd7g6VpH/w+9jLvm+7tOwfMQZlXp9104t9XMVnTAchzQr6mB3U6drCsGnuZycQzEgretQsUh3hweN7Jzz5knl6qc1n3Sn8t1yOvaIQLWG1f3l6irPdl28bwEd4Z7VDrGqYgXsd2GsOK/gCQ7rChNqbJ2p+jCja3F3ZohfmTYOU8W7DJ8Ne+xaofSuPnWODnZN9x+Y+3RE3nzH9tzP+NBMsV3YQXpvUD7Pepg7ScO+k9Fj3/F+KfBje0k6xfl+75s7kR3pNWQI5EVrO6iuky6dMuFPUBfNfq33fZV6Tqr/7o24aKpfA4WwJf91G9mC18z8NCgFR6iK4cPGmkTMvNtxUQ3MoB0LCOkRcbP0i7qxHupt8xE=",
                            money, UUID.randomUUID());

            } else if (plugin.getConfigManager().dropMoneyOnGroundItemtype.equalsIgnoreCase("SKULL")) {
                uuid = UUID.fromString(MH_REWARD_BAG_OF_GOLD_UUID);
                is = customItems.getCustomtexture(uuid, plugin.getConfigManager().dropMoneyOnGroundSkullRewardName,
                        plugin.getConfigManager().dropMoneyOnGroundSkullTextureValue,
                        plugin.getConfigManager().dropMoneyOnGroundSkullTextureSignature, money, UUID.randomUUID());

            } else if (plugin.getConfigManager().dropMoneyOnGroundItemtype.equalsIgnoreCase("KILLER")) {
                uuid = UUID.fromString(MH_REWARD_KILLER_UUID);
                is = customItems.getPlayerHead(player.getName(), money);

            } else { // ITEM
                uuid = UUID.fromString(MH_REWARD_ITEM_UUID);
                is = new ItemStack(Material.valueOf(plugin.getConfigManager().dropMoneyOnGroundItem), 1);
            }

            item = location.getWorld().dropItem(location, is);
            this.getDroppedMoney().put(item.getEntityId(), money);
            item.setMetadata(MH_REWARD_DATA,
                    new FixedMetadataValue(MobHunting.getInstance(),
                            new Reward(
                                    plugin.getConfigManager().dropMoneyOnGroundItemtype.equalsIgnoreCase("ITEM")
                                            ? "" : Reward.getReward(is).getDisplayname(),
                                    money, uuid, UUID.randomUUID())));
            if (Misc.isMC18OrNewer()) {
                item.setCustomName(ChatColor.valueOf(plugin.getConfigManager().dropMoneyOnGroundTextColor)
                        + plugin.getRewardManager().format(money));
                item.setCustomNameVisible(true);
            }
        }
        if (item != null)
            messages.debug("%s was dropped on the ground as item %s (# of rewards=%s)",
                    plugin.getRewardManager().format(money),
                    plugin.getConfigManager().dropMoneyOnGroundItemtype, droppedMoney.size());
    }

    public void saveReward(UUID uuid) {
        try {
            config.options().header("This is the rewards placed as blocks. Do not edit this file manually!");
            if (placedMoney_Reward.containsKey(uuid)) {
                Location location = placedMoney_Location.get(uuid);
                if (location != null && location.getBlock().getType() == Material.SKULL) {
                    Reward reward = placedMoney_Reward.get(uuid);
                    ConfigurationSection section = config.createSection(uuid.toString());
                    section.set("location", location);
                    reward.save(section);
                    messages.debug("Saving a reward placed as a block.");
                    config.save(file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadAllStoredRewards() {
        int n = 0;
        int deleted = 0;
        try {

            if (!file.exists())
                return;

            config.load(file);

        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        try {
            for (String key : config.getKeys(false)) {
                ConfigurationSection section = config.getConfigurationSection(key);
                Reward reward = new Reward();
                reward.read(section);
                Location location = (Location) section.get("location");
                if (location != null && location.getBlock().getType() == Material.SKULL) {
                    location.getBlock().setMetadata(MH_REWARD_DATA,
                            new FixedMetadataValue(MobHunting.getInstance(), new Reward(reward)));
                    placedMoney_Reward.put(UUID.fromString(key), reward);
                    placedMoney_Location.put(UUID.fromString(key), location);
                    n++;
                } else {
                    deleted++;
                    config.set(key, null);
                }
            }
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        try {

            if (deleted > 0) {
                messages.debug("Deleted %s rewards from the rewards.yml file", deleted);
                File file_copy = new File(MobHunting.getInstance().getDataFolder(), "rewards.yml.old");
                Files.copy(file.toPath(), file_copy.toPath(), StandardCopyOption.COPY_ATTRIBUTES,
                        StandardCopyOption.REPLACE_EXISTING);
                config.save(file);
            }
            if (n > 0) {
                messages.debug("Loaded %s rewards from the rewards.yml file", n);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ItemStack setDisplayNameAndHiddenLores(ItemStack skull, String mDisplayName, double money,
                                                  UUID uuid) {
        ItemMeta skullMeta = skull.getItemMeta();
        skullMeta.setLore(new ArrayList<String>(Arrays.asList("Hidden:" + mDisplayName, "Hidden:" + money,
                "Hidden:" + uuid, money == 0 ? "Hidden:" : "Hidden:" + UUID.randomUUID())));
        if (money == 0)
            skullMeta.setDisplayName(
                    ChatColor.valueOf(plugin.getConfigManager().dropMoneyOnGroundTextColor) + mDisplayName);
        else
            skullMeta.setDisplayName(ChatColor.valueOf(plugin.getConfigManager().dropMoneyOnGroundTextColor)
                    + (plugin.getConfigManager().dropMoneyOnGroundItemtype.equalsIgnoreCase("ITEM")
                    ? plugin.getRewardManager().format(money)
                    : mDisplayName + " (" + plugin.getRewardManager().format(money) + ")"));
        skull.setItemMeta(skullMeta);
        return skull;
    }

}
