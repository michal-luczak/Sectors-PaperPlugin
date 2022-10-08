package me.taison.sectors;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class PlayerDataToTransfer implements Serializable {

    private int x,y,z;
    private UUID uuid;
    private int serverPort;
    private final HashMap<Integer, ImmutablePair<Material, Integer>> items = new HashMap<>();
    private final HashMap<Integer, ImmutablePair<Material, Integer>> enderchest = new HashMap<>();
    private final HashMap<EquipmentSlot, Material> armor = new HashMap<>();
    private HashSet<Map<String, Object>> effects;
    private float yaw;
    private float health;
    private int hunger;

    public PlayerDataToTransfer(Player player, int serverPort) {
        hunger = player.getFoodLevel();
        health = (float) player.getHealth();
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
