package me.taison.sectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerDataToTransfer implements Serializable {

    private int x,y,z;
    private UUID uuid;
    private int serverPort;
    private final HashMap<Integer, ImmutablePair<Material, Integer>> items = new HashMap<>();
    private final HashMap<Integer, ImmutablePair<Material, Integer>> enderchest = new HashMap<>();
    private final HashMap<EquipmentSlot, Material> armor = new HashMap<>();
    private HashSet<Map<String, Object>> effects;

    public HashSet<Map<String, Object>> getEffects() {
        return effects;
    }

    public void setEffects(HashSet<Map<String, Object>> effects) {
        this.effects = effects;
    }

    public HashMap<Integer, ImmutablePair<Material, Integer>> getItems() {
        return items;
    }

    public HashMap<EquipmentSlot, Material> getArmor() {
        return armor;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    private float yaw;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public HashMap<Integer, ImmutablePair<Material, Integer>> getEnderchest() {
        return enderchest;
    }

    public PlayerDataToTransfer(Player player, int serverPort) {
        x = player.getLocation().getBlockX();
        y = player.getLocation().getBlockY();
        z = player.getLocation().getBlockZ();
        uuid = player.getUniqueId();
        this.serverPort = serverPort;
        PlayerInventory inventory = player.getInventory();
        for (int i=0; i<inventory.getSize(); i++) {
            if (inventory.getItem(i) != null)
                items.put(i, new ImmutablePair<>(inventory.getItem(i).getType(), inventory.getItem(i).getAmount()));
        }

        if (inventory.getBoots() != null)
            armor.put(EquipmentSlot.FEET, inventory.getBoots().getType());
        if (inventory.getLeggings() != null)
            armor.put(EquipmentSlot.LEGS, inventory.getLeggings().getType());
        if (inventory.getChestplate() != null)
            armor.put(EquipmentSlot.CHEST, inventory.getChestplate().getType());
        if (inventory.getHelmet() != null)
            armor.put(EquipmentSlot.HEAD, inventory.getHelmet().getType());

        yaw = player.getLocation().getYaw();

        effects = player.getActivePotionEffects().stream().map(PotionEffect::serialize).collect(Collectors.toCollection(HashSet::new));

        for (int i=0; i<player.getEnderChest().getSize(); i++) {
            if (player.getEnderChest().getItem(i) != null)
                enderchest.put(i, new ImmutablePair<>(player.getEnderChest().getItem(i).getType(), player.getEnderChest().getItem(i).getAmount()));
        }
    }
}
