package one.lindegaard.MobHunting.npc;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.compatibility.CitizensCompat;
import one.lindegaard.MobHunting.util.Misc;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.material.PistonExtensionMaterial;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MasterMobHunterSign implements Listener {

    static final String MH_SIGN = "MH:sign";
    final static byte POWER_FROM_SIGN = 15;
    private final static String MH_POWERED = "MH:powered";
    private final static BlockFace possibleBlockface[] = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST,
            BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
    // https://regex101.com/
    // Regex string="\[(MH|mh|Mh|mH)(\d+)(\+)?\]"
    // Example: [mh001+]
    final String MASTERMOBHUNTERSIGN = "\\[(MH|mh|Mh|mH)(\\d+)(\\+)?\\]";

    public List<Material> supportedmats = new ArrayList<Material>();
    private MobHunting plugin;
    private Messages messages;

    public MasterMobHunterSign(MobHunting plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();

        supportedmats.add(Material.REDSTONE_COMPARATOR_OFF);
        supportedmats.add(Material.REDSTONE_COMPARATOR_ON);
        supportedmats.add(Material.REDSTONE_LAMP_OFF);
        supportedmats.add(Material.REDSTONE_LAMP_ON);
        supportedmats.add(Material.REDSTONE_TORCH_OFF);
        supportedmats.add(Material.REDSTONE_TORCH_ON);
        supportedmats.add(Material.REDSTONE_WIRE);
        supportedmats.add(Material.DISPENSER);
        supportedmats.add(Material.FURNACE);
        supportedmats.add(Material.POWERED_RAIL);
        supportedmats.add(Material.ACTIVATOR_RAIL);
        supportedmats.add(Material.DIODE_BLOCK_OFF);
        supportedmats.add(Material.DIODE_BLOCK_ON);
        supportedmats.add(Material.COMMAND);
        supportedmats.add(Material.FENCE_GATE);
        supportedmats.add(Material.IRON_DOOR);
        supportedmats.add(Material.WOODEN_DOOR);
        supportedmats.add(Material.JUKEBOX);
        supportedmats.add(Material.PISTON_BASE);
        supportedmats.add(Material.PISTON_STICKY_BASE);
        supportedmats.add(Material.TNT);
        supportedmats.add(Material.TRAP_DOOR);
    }

    // ****************************************************************************'
    // SETTERS
    // ****************************************************************************'

    @SuppressWarnings("deprecation")
    private static void setMHPowerOnRedstoneWire(Block block, byte power) {
        block.setTypeIdAndData(Material.REDSTONE_WIRE.getId(), power, true);
        block.getState().update();
    }

    @SuppressWarnings("deprecation")
    private static void setPowerOnPiston(Block b) {
        PistonBaseMaterial pistonData = (PistonBaseMaterial) b.getState().getData();
        if (!pistonData.isPowered()) {
            pistonData.setPowered(true);
            b.setData(pistonData.getData(), false);
            b.getState().update();

            BlockFace blockFace = pistonData.getFacing();
            Block tb = b.getRelative(blockFace);
            tb.setType(Material.PISTON_EXTENSION, false);
            PistonExtensionMaterial pistonExtentionData = (PistonExtensionMaterial) tb.getState().getData();
            pistonExtentionData.setFacingDirection(b.getFace(tb));
            tb.setData(pistonExtentionData.getData(), false);
            tb.getState().update();
        }
    }

    @SuppressWarnings("deprecation")
    private static void removePowerOnPiston(Block b) {
        PistonBaseMaterial pistonData = (PistonBaseMaterial) b.getState().getData();
        if (!pistonData.isPowered()) {
            pistonData.setPowered(false);
            b.setData(pistonData.getData(), false);
            b.getState().update();

            BlockFace blockFace = pistonData.getFacing();
            Block tb = b.getRelative(blockFace);
            tb.setType(Material.PISTON_EXTENSION, false);
            PistonExtensionMaterial pistonExtentionData = (PistonExtensionMaterial) tb.getState().getData();
            pistonExtentionData.setFacingDirection(b.getFace(tb));
            tb.setData(pistonExtentionData.getData(), false);
            tb.getState().update();
        }
    }

    public void setPower(Block b, byte power) {
        if (isMHSign(b)) {
            if (isPowerSetOnSign(b))
                setMHPower(b, POWER_FROM_SIGN);
            else
                removeMHPower(b);
        } else if (isRedstoneWire(b)) {
            setMHPowerOnRedstoneWire(b, power);
        }
    }

    private  void setMHPower(Block b, byte power) {
        if (power >= 0 && power <= POWER_FROM_SIGN) {
            b.setMetadata(MH_POWERED, new FixedMetadataValue(MobHunting.getInstance(), power));
            if (isRedstoneWire(b))
                setMHPowerOnRedstoneWire(b, power);
        }
        if (isMHSign(b) || isMHIndirectPoweredBySign(b)) {
            power = POWER_FROM_SIGN;
        } else {
            power--;
        }
        if (power >= 0 && power <= POWER_FROM_SIGN) {
            for (BlockFace bf : possibleBlockface) {
                Block rb = b.getRelative(bf);
                if (supportedmats.contains(rb.getType())) {
                    if (isMHIndirectPoweredBySign(rb)) {
                        if (isRedstoneWire(rb)) {
                            setMHPowerOnRedstoneWire(rb, power);
                        } else if (isRedstoneLamp(rb)) {
                            setPowerOnRedstoneLamp(rb, power);
                        } else if (isPistonBase(rb)) {
                            setPowerOnPiston(rb);
                        }
                        if (!isMHPowered(rb))
                            setMHPower(rb, power);
                    }
                }
            }
        }
    }

    private void setPowerOnRedstoneLamp(Block lamp, byte power) {
        if (lamp.getType().equals(Material.REDSTONE_LAMP_OFF) && isMHIndirectPoweredBySign(lamp)) {
            for (BlockFace bf : possibleBlockface) {
                Block rb = lamp.getRelative(bf);
                if (isMHPoweredSign(rb)) {
                    Material signType = rb.getType();
                    Sign sign = ((Sign) rb.getState());
                    MaterialData md = sign.getData();
                    String[] copyOfSigntext = sign.getLines();
                    rb.setType(Material.REDSTONE_TORCH_ON);
                    rb.setTypeIdAndData(signType.getId(), md.getData(), false);
                    Sign newSign = ((Sign) rb.getState());
                    for (int i = 0; i < 4; i++) {
                        newSign.setLine(i, copyOfSigntext[i]);
                    }
                    newSign.update();
                }
            }
        }
    }

    // ****************************************************************************'
    // GETTERS
    // ****************************************************************************'

    public  int getNPCIdOnSign(Block block) {
        if (!Misc.isSign(block))
            return -1;
        String str = ((Sign) block.getState()).getLine(0);

        if (str.matches(MASTERMOBHUNTERSIGN)) {
            // block.setMetadata(MH_SIGN, new
            // FixedMetadataValue(MobHunting.getInstance(), str));
            // Messages.debug("(186)MH Sign updated=%s",
            // str);

            // TODO: cleanup
        } else if (block.hasMetadata(MH_SIGN)) {
            String md = block.getMetadata(MH_SIGN).get(0).asString();
            if (md.matches(MASTERMOBHUNTERSIGN))
                str = md;
        }
        if (str.matches(MASTERMOBHUNTERSIGN)) {
            Pattern pattern = Pattern.compile(MASTERMOBHUNTERSIGN);
            Matcher m = pattern.matcher(str);
            m.find();
            return Integer.valueOf(m.group(2));
        } else
            return -1;
    }

    public  int getNPCIdOnSign(String str) {
        if (str.matches(MASTERMOBHUNTERSIGN)) {
            Pattern pattern = Pattern.compile(MASTERMOBHUNTERSIGN);
            Matcher m = pattern.matcher(str);
            m.find();
            return Integer.valueOf(m.group(2));
        } else
            return -1;
    }

    public boolean isPowerSetOnSign(Block block) {
        String str;
        if (isMHSign(block)) {
            if (block.hasMetadata(MH_SIGN))
                str = block.getMetadata(MH_SIGN).get(0).asString();
            else
                str = ((Sign) block.getState()).getLine(0);
            if (str.matches(MASTERMOBHUNTERSIGN)) {
                Pattern pattern = Pattern.compile(MASTERMOBHUNTERSIGN);
                Matcher m = pattern.matcher(str);
                m.find();
                return m.group(3) != null;
            }
        }
        return false;
    }

    // ****************************************************************************'
    // REMOVE
    // ****************************************************************************'

    public void removePower(Block block) {
        if (isMHPowered(block)) {
            block.removeMetadata(MH_POWERED, MobHunting.getInstance());
            for (BlockFace bf : possibleBlockface) {
                Block rb = block.getRelative(bf);
                //Messages.debug("rb = %s, isPowered=%s, !isMHPoweredSign=%s", rb.getType(), isMHPowered(rb),
                //		!isMHPoweredSign(rb));
                if (rb != null && isMHPowered(rb) && !isMHPoweredSign(rb) && supportedmats.contains(rb.getType())) {
                    //Messages.debug("remove power on %s", rb.getType());
                    if (rb.getType().equals(Material.REDSTONE_LAMP_ON)) {
                        rb.setType(Material.REDSTONE_LAMP_OFF);
                        //Messages.debug("Turn Redstone Lamp OFF");
                        //BlockRedstoneEvent bre = new BlockRedstoneEvent(rb, 15, 0);
                        //Bukkit.getServer().getPluginManager().callEvent(bre);
                    } else if (rb.getType().equals(Material.REDSTONE_WIRE)) {
                        rb.setTypeIdAndData(Material.REDSTONE_WIRE.getId(), (byte) 0, true);
                        rb.getState().update();
                        //BlockRedstoneEvent bre = new BlockRedstoneEvent(rb, 15, 0);
                        //Bukkit.getServer().getPluginManager().callEvent(bre);
                    } else if (rb.getType().equals(Material.PISTON_BASE)
                            || (rb.getType().equals(Material.PISTON_STICKY_BASE))) {
                        removePowerOnPiston(rb);
                        //BlockRedstoneEvent bre = new BlockRedstoneEvent(rb, 15, 0);
                        //Bukkit.getServer().getPluginManager().callEvent(bre);
                    }
                    removeMHPower(rb);
                    // BlockRedstoneEvent bre = new BlockRedstoneEvent(rb, 15,
                    // 0);
                    // Bukkit.getServer().getPluginManager().callEvent(bre);
                }
            }
        }

    }

    private void removeMHPower(Block block) {
        block.removeMetadata(MH_POWERED, MobHunting.getInstance());
        for (BlockFace bf : possibleBlockface) {
            Block rb = block.getRelative(bf);
            if (isMHPowered(rb) && !isMHPoweredSign(rb))
                removeMHPower(rb);
        }
    }

    // ****************************************************************************'
    // Events
    // ****************************************************************************'
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.isCancelled())
            return;
        if (event.getClickedBlock() == null)
            return;

        // fails on some 1.9 servers???
        //if (Misc.isMC19OrNewer() && (event.getHand() == null || event.getHand().equals(EquipmentSlot.OFF_HAND)))
        //	return;

        if (event.getPlayer().getItemInHand().getType().equals(Material.STICK)) {

            boolean turnon = (event.getAction() == Action.LEFT_CLICK_BLOCK);

            // Check if Block is powered or indirectly powered
            int power = 0;
            if (event.getClickedBlock().hasMetadata(MH_POWERED)) {
                for (MetadataValue mdv : event.getClickedBlock().getMetadata(MH_POWERED)) {
                    int p = mdv.asInt();
                    power = power > p ? power : p;
                }
            }

            messages.debug("power=%s, hasMeta(MH_POWERED)=%s", power, event.getClickedBlock().hasMetadata(MH_POWERED));

            // Check if block is MMH Sign
            if (isMHSign(event.getClickedBlock())) {
                if (event.getPlayer().getItemInHand().getType().equals(Material.STICK)) {
                    int id = getNPCIdOnSign(event.getClickedBlock());
                    if (id != -1) {
                        if (power > 0)
                            messages.playerActionBarMessage(event.getPlayer(),
                                    messages.getString("mobhunting.npc.clickednpcsignpowered", "npcid", id));
                        else
                            messages.playerActionBarMessage(event.getPlayer(),
                                    messages.getString("mobhunting.npc.clickednpcsign", "npcid", id));

                        NPC npc = CitizensAPI.getNPCRegistry().getById(id);
                        if (npc != null) {
                            if (MasterMobHunterManager.isMasterMobHunter(npc)) {

                                MasterMobHunter mmh = MasterMobHunterManager.getMasterMobHunterManager()
                                        .get(npc.getId());

                                if (isMHSign(((org.bukkit.block.Sign) event.getClickedBlock().getState()).getLine(0))) {
                                    event.getClickedBlock().setMetadata(MH_SIGN, new FixedMetadataValue(
                                            MobHunting.getInstance(),
                                            ((org.bukkit.block.Sign) event.getClickedBlock().getState()).getLine(0)));
                                    event.getClickedBlock().getState().setMetadata(MH_SIGN,
                                            new FixedMetadataValue(MobHunting.getInstance(),
                                                    ((org.bukkit.block.Sign) event.getClickedBlock().getState())
                                                            .getLine(0)));
                                    mmh.putSignLocation(event.getClickedBlock().getLocation());
                                    MasterMobHunterManager.getMasterMobHunterManager().put(id, mmh);
                                }
                                ((org.bukkit.block.Sign) event.getClickedBlock().getState()).setLine(1,
                                        Misc.trimSignText(mmh.getRank() + "." + npc.getName()));
                                ((org.bukkit.block.Sign) event.getClickedBlock().getState()).setLine(2,
                                        Misc.trimSignText(mmh.getPeriod().translateNameFriendly()));
                                ((org.bukkit.block.Sign) event.getClickedBlock().getState()).setLine(3,
                                        Misc.trimSignText(
                                                mmh.getNumberOfKills() + " " + mmh.getStatType().translateName()));

                                if (turnon) {
                                    setPower(event.getClickedBlock(), MasterMobHunterSign.POWER_FROM_SIGN);
                                } else {
                                    boolean powered = isPowerSetOnSign(event.getClickedBlock());
                                    if (powered) {
                                        OfflinePlayer offlinePlayer = Bukkit.getPlayer(npc.getName());
                                        if (offlinePlayer != null && offlinePlayer.isOnline()) {
                                            setPower(event.getClickedBlock(), MasterMobHunterSign.POWER_FROM_SIGN);
                                        } else {
                                            removePower(event.getClickedBlock());
                                        }
                                    } else
                                        removePower(event.getClickedBlock());
                                    MasterMobHunterManager.getMasterMobHunterManager().get(npc.getId()).update();
                                }
                            }
                        }
                    } else {
                        ((org.bukkit.block.Sign) event.getClickedBlock().getState()).setLine(1,
                                "Id=" + id + " is not a");
                        ((org.bukkit.block.Sign) event.getClickedBlock().getState()).setLine(2, "MasterMobHunter");
                        ((org.bukkit.block.Sign) event.getClickedBlock().getState()).setLine(3, "");
                        event.getClickedBlock().getState().update();
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlaceEvent(BlockPlaceEvent e) {
        // BlockPlaceEvent is called before the player enter the text on the
        // sign
        Block b = e.getBlock();
        if (isRedstoneWire(b)) {
            if (isMHIndirectPoweredBySign(b)) {
                // power on Redstone must be set immediately to work
                setMHPower(b, POWER_FROM_SIGN);
                b.setData(POWER_FROM_SIGN, true);
                b.getState().update();
            }
        } else if ((isRedstoneLamp(b) || isPistonBase(b)) && isMHIndirectPoweredBySign(b)) {
            // power on Redstone Lamp and Piston must be set in next tick to
            // work
            setMHPowerLater(b);
        }

    }

    @EventHandler
    public void onSignChangeEvent(SignChangeEvent event) {
        Player player = event.getPlayer();
        Block sb = event.getBlock();
        if (isMHSign(sb) || isMHSign(event.getLine(0))) {
            int id = getNPCIdOnSign(event.getLine(0));
            if (id != -1) {
                boolean powered = isPowerSetOnSign(sb);
                NPC npc = CitizensAPI.getNPCRegistry().getById(id);
                if (npc != null) {
                    if (MasterMobHunterManager.isMasterMobHunter(npc)) {
                        MasterMobHunter mmh = MasterMobHunterManager.getMasterMobHunterManager().get(npc.getId());

                        if (isMHSign(event.getLine(0))) {
                            sb.setMetadata(MH_SIGN, new FixedMetadataValue(MobHunting.getInstance(), event.getLine(0)));
                            sb.getState().setMetadata(MH_SIGN, new FixedMetadataValue(MobHunting.getInstance(),
                                    ((org.bukkit.block.Sign) sb.getState()).getLine(0)));
                            mmh.putSignLocation(sb.getLocation());
                            MasterMobHunterManager.getMasterMobHunterManager().put(id, mmh);
                            messages.playerActionBarMessage(player,
                                    player.getName() + " placed a MobHunting Sign (ID=" + id + ")");
                        }

                        event.setLine(1, Misc.trimSignText(mmh.getRank() + "." + npc.getName()));
                        event.setLine(2, Misc.trimSignText(mmh.getPeriod().translateNameFriendly()));
                        event.setLine(3,
                                Misc.trimSignText(mmh.getNumberOfKills() + " " + mmh.getStatType().translateName()));

                        if (powered) {
                            OfflinePlayer offlinePlayer = Bukkit.getPlayer(npc.getName());
                            if (offlinePlayer != null && offlinePlayer.isOnline() && !CitizensCompat.isNPC(player))
                                setPower(event.getBlock(), MasterMobHunterSign.POWER_FROM_SIGN);
                            else
                                removePower(event.getBlock());
                        } else
                            removePower(event.getBlock());
                        MasterMobHunterManager.getMasterMobHunterManager().get(npc.getId()).update();
                    } else {
                        event.setLine(1, "ID=" + id + " is not a");
                        event.setLine(2, "MasterMobHunter");
                        event.setLine(3, "");
                    }
                } else {
                    event.setLine(1, "Invalid npc id");
                    event.setLine(2, "");
                    event.setLine(3, "");
                }
            } else {
                messages.debug("The sign does not have a valid NPC id!(%s)", id);
            }
        }

    }

    private void setMHPowerLater(final Block block) {
        Bukkit.getScheduler().runTaskLater(MobHunting.getInstance(), new Runnable() {
            @Override
            public void run() {
                if (isRedstoneLamp(block))
                    setPowerOnRedstoneLamp(block, POWER_FROM_SIGN);
                else if (isPistonBase(block))
                    setPowerOnPiston(block);
                block.setMetadata(MH_POWERED, new FixedMetadataValue(plugin, (byte) 15));
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockRedstoneEvent(BlockRedstoneEvent e) {
        Block b = e.getBlock();
        if (isMHPowered(b)) {
            for (MetadataValue mdv : b.getMetadata(MH_POWERED)) {
                if (isMHIndirectPoweredBySign(e.getBlock()))
                    e.setNewCurrent(mdv.asInt());
                else {
                    removeMHPower(b);
                    e.setNewCurrent(0);
                }
                if (mdv.asInt() == 0) {
                    removeMHPower(b);
                    for (BlockFace bf : possibleBlockface) {
                        Block rb = b.getRelative(bf);
                        removeMHPower(rb);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockPhysicsEvent(final BlockPhysicsEvent e) {

        // This is the block which is going to be changed
        Block b = e.getBlock();
        // getChangedType() the type of block that changed, causing this
        // event
        Material c = e.getChangedType();

        if (b.getType().equals(Material.REDSTONE_LAMP_ON)) {
            if (isMHIndirectPoweredBySign(b)) {
                e.setCancelled(true);
            }
        } else if (isPiston(b)) {
            if (isMHIndirectPoweredBySign(b))
                if (isMHPowered(b)) {
                    e.setCancelled(true);
                    setMHPowerLater(b);
                }
            if ((b.getType().equals(Material.PISTON_EXTENSION) && c.equals(Material.REDSTONE_WIRE))) {

            }
        }
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent e) {
        Block b = e.getBlock();
        if (isMHPowered(b)) {
            removeMHPower(b);
        }
        if (isMHSign(b)) {
            int id = getNPCIdOnSign(b);
            if (id != -1) {
                MasterMobHunterManager.getMasterMobHunterManager().get(id).removeLocation(e.getBlock().getLocation());
            }
        }
    }

    // ************************************************************************************
    // TESTS
    // ************************************************************************************

    public boolean isRedstoneWire(Block block) {
        return block.getType().equals(Material.REDSTONE_WIRE);
    }

    public boolean isRedstoneLamp(Block block) {
        return block.getType().equals(Material.REDSTONE_LAMP_OFF) || block.getType().equals(Material.REDSTONE_LAMP_ON);
    }

    public boolean isPiston(Block block) {
        return block.getType().equals(Material.PISTON_BASE) || block.getType().equals(Material.PISTON_EXTENSION)
                || block.getType().equals(Material.PISTON_MOVING_PIECE)
                || block.getType().equals(Material.PISTON_STICKY_BASE);
    }

    public boolean isPistonBase(Block block) {
        return block.getType().equals(Material.PISTON_BASE) || block.getType().equals(Material.PISTON_STICKY_BASE);
    }

    public boolean isMHSign(Block block) {
        if (Misc.isSign(block)) {
            Sign sign = (Sign) block.getState();
            if (sign.getLine(0).matches(MASTERMOBHUNTERSIGN))
                return true;
            else if (block.hasMetadata(MH_SIGN))
                return true;
            else if (sign.hasMetadata(MH_SIGN))
                return true;
        }
        return false;
    }

    public boolean isMHSign(String line) {
        return line.matches(MASTERMOBHUNTERSIGN);
    }

    private boolean isMHPoweredSign(Block block) {
        return isMHSign(block) && isMHPowered(block);
    }

    private boolean isMHPowered(Block block) {
        return block.hasMetadata(MH_POWERED);
    }

    private boolean isMHIndirectPoweredBySign(Block block) {
        for (BlockFace bf : possibleBlockface) {
            if (isMHPoweredSign(block.getRelative(bf)))
                return true;
        }
        return false;
    }

}
