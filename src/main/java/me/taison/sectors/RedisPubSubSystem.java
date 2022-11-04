package me.taison.sectors;

import org.apache.commons.lang3.SerializationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RedisPubSubSystem {

    private final Jedis jedisForPublish = new Jedis();
    private final Jedis jedisForSubscribe = new Jedis();
    private boolean isRunning;

    public void subscribeChannel() {
        try {

            BinaryJedisPubSub binaryJedisPubSub = new BinaryJedisPubSub() {

                @Override
                public void onMessage(byte[] channel, byte[] message) {
                    if (Arrays.equals(channel, new byte[]{9})) {
                        PlayerDataToTransfer playerDataToTransfer = SerializationUtils.deserialize(message);
                        if (playerDataToTransfer.getServerPort() == Bukkit.getServer().getPort())
                            return;
                        Location location = new Location(Bukkit.getWorld("world"),
                                playerDataToTransfer.getX(),
                                playerDataToTransfer.getY(),
                                playerDataToTransfer.getZ());
                        location.setYaw(playerDataToTransfer.getYaw());
                        while(true) {
                            if (Bukkit.getPlayer(playerDataToTransfer.getUuid()) != null) {
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        if (isRunning) {
                                            cancel();
                                            return;
                                        }

                                        isRunning = true;

                                        if (Bukkit.getScheduler().isQueued(getTaskId()))
                                            cancel();

                                        Player player = Bukkit.getPlayer(playerDataToTransfer.getUuid());
                                        if (player.getPassengers().isEmpty())
                                            player.teleport(location);
                                        else {
                                            List<Entity> passengers = player.getPassengers();
                                            player.getPassengers().forEach(player::removePassenger);
                                            player.teleport(location);
                                            passengers.forEach(player::addPassenger);
                                        }

                                        player.getInventory().clear();
                                        for (Integer i : playerDataToTransfer.getItems().keySet()) {
                                            player.getInventory().setItem(i, new ItemStack(playerDataToTransfer.getItems().get(i).left,
                                                    playerDataToTransfer.getItems().get(i).right));
                                        }

                                        if (playerDataToTransfer.getArmor().get(EquipmentSlot.FEET) != null)
                                            player.getInventory().setBoots(new ItemStack(playerDataToTransfer.getArmor().get(EquipmentSlot.FEET)));
                                        if (playerDataToTransfer.getArmor().get(EquipmentSlot.LEGS) != null)
                                            player.getInventory().setLeggings(new ItemStack(playerDataToTransfer.getArmor().get(EquipmentSlot.LEGS)));
                                        if (playerDataToTransfer.getArmor().get(EquipmentSlot.CHEST) != null)
                                            player.getInventory().setChestplate(new ItemStack(playerDataToTransfer.getArmor().get(EquipmentSlot.CHEST)));
                                        if (playerDataToTransfer.getArmor().get(EquipmentSlot.HEAD) != null)
                                            player.getInventory().setHelmet(new ItemStack(playerDataToTransfer.getArmor().get(EquipmentSlot.HEAD)));

                                        for (PotionEffect activePotionEffect : player.getActivePotionEffects()) {
                                            player.removePotionEffect(activePotionEffect.getType());
                                        }

                                        player.addPotionEffects(playerDataToTransfer.getEffects().stream().map(PotionEffect::new).collect(Collectors.toSet()));

                                        player.getEnderChest().clear();
                                        for (Integer i : playerDataToTransfer.getEnderchest().keySet()) {
                                            player.getEnderChest().setItem(i, new ItemStack(playerDataToTransfer.getEnderchest().get(i).left,
                                                    playerDataToTransfer.getEnderchest().get(i).right));
                                        }

                                        player.setFoodLevel(playerDataToTransfer.getHunger());
                                        player.setHealth(playerDataToTransfer.getHealth());
                                        player.getInventory().setHeldItemSlot(playerDataToTransfer.getHeldItemSlot());
                                        isRunning = false;
                                    }
                                }.runTask(Sectors.getPlugin(Sectors.class));

                                break;
                            }
                        }
                    }
                }

                @Override
                public void onSubscribe(byte[] channel, int subscribedChannels) {
                    System.out.println("Subscribed " + Arrays.toString(channel) + " channel");
                }
            };

            jedisForSubscribe.subscribe(binaryJedisPubSub, new byte[]{9});

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publish(Serializable object, byte numberOfChannel) {

        try {
            jedisForPublish.publish(new byte[]{numberOfChannel}, SerializationUtils.serialize(object));
        } catch (Exception ex) {
            System.out.println("Exception : " + ex.getMessage());
        }
    }
}