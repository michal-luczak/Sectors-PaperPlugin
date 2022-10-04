package me.taison.sectors;

import org.apache.commons.lang3.SerializationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;

import java.util.Arrays;

public class RedisPubSubSystem {

    private static final Jedis jedisForPublish = new Jedis();
    private static final Jedis jedisForSubscribe = new Jedis();

    public static void subscribeChannel() {

        try {

            BinaryJedisPubSub binaryJedisPubSub = new BinaryJedisPubSub() {

                @Override
                public void onMessage(byte[] channel, byte[] message) {
                    if (Arrays.equals(channel, new byte[]{9})) {
                        SerializableLocation serializableLocation = SerializationUtils.deserialize(message);
                        if (serializableLocation.getServerPort() == Bukkit.getServer().getPort())
                            return;
                        Location location = new Location(Bukkit.getWorld("world"),
                                serializableLocation.getX(),
                                serializableLocation.getY(),
                                serializableLocation.getZ());
                        location.setYaw(serializableLocation.getYaw());
                        while(true) {
                            if (Bukkit.getPlayer(serializableLocation.getUuid()) != null) {
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        Bukkit.getPlayer(serializableLocation.getUuid()).teleport(location);
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

    public static void publish(SerializableLocation serializableLocation) {

        try {
            jedisForPublish.publish(new byte[]{9}, SerializationUtils.serialize(serializableLocation));
        } catch (Exception ex) {
            System.out.println("Exception : " + ex.getMessage());
        }
    }
}