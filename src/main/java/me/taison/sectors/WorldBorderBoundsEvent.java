package me.taison.sectors;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Optional;

public class WorldBorderBoundsEvent implements Listener, PluginMessageListener {

    private static final double boundW = Sectors.getPlugin(Sectors.class).getConfig().getInt("bound_W");
    private static final double boundE = Sectors.getPlugin(Sectors.class).getConfig().getInt("bound_E");
    private static final double boundS = Sectors.getPlugin(Sectors.class).getConfig().getInt("bound_S");
    private static final double boundN = Sectors.getPlugin(Sectors.class).getConfig().getInt("bound_N");

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        int playerX = player.getLocation().getBlockX();
        int playerZ = player.getLocation().getBlockZ();

        Server serverName = Server.valueOf(Sectors.getPlugin(Sectors.class).getConfig().getString("server_name").toUpperCase());

        if (getBound(player.getLocation()).isEmpty())
            return;

        Server serverToConnectName = Server.SPAWN;
        SerializableLocation serializableLocation = new SerializableLocation(player.getLocation(), player.getUniqueId(), Bukkit.getServer().getPort());
        Bound bound = getBound(player.getLocation()).get();

        switch (serverName) {
            case SPAWN -> {
                switch (bound) {
                    case W -> {
                        serverToConnectName = Server.SECTOR_WEST;
                        serializableLocation.setX(serializableLocation.getX() - 2);
                    }
                    case E -> {
                        serverToConnectName = Server.SECTOR_EAST;
                        serializableLocation.setX(serializableLocation.getX() + 2);
                    }
                    case S -> {
                        serverToConnectName = Server.SECTOR_SOUTH;
                        serializableLocation.setZ(serializableLocation.getZ() + 2);
                    }
                    case N -> {
                        serverToConnectName = Server.SECTOR_NORTH;
                        serializableLocation.setZ(serializableLocation.getZ() - 2);
                    }
                }
            }
            case SECTOR_WEST -> {
                switch (bound) {
                    case W, N -> {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Dotarłeś do granicy mapy!"));
                        return;
                    }
                    case E -> {
                        if (playerZ >= -75) {
                            serverToConnectName = Server.SPAWN;
                        }
                        else
                            serverToConnectName = Server.SECTOR_NORTH;
                        serializableLocation.setX(serializableLocation.getX() + 2);
                    }
                    case S -> {
                        serverToConnectName = Server.SECTOR_SOUTH;
                        serializableLocation.setZ(serializableLocation.getZ() + 2);
                    }
                }
            }
            case SECTOR_SOUTH -> {
                switch (bound) {
                    case W, S -> {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Dotarłeś do granicy mapy!"));
                        return;
                    }
                    case E -> {
                        serverToConnectName = Server.SECTOR_EAST;
                        serializableLocation.setX(serializableLocation.getX() + 2);
                    }
                    case N -> {
                        if (playerX >= -75)
                            serverToConnectName = Server.SPAWN;
                        else
                            serverToConnectName = Server.SECTOR_WEST;
                        serializableLocation.setZ(serializableLocation.getZ() - 2);
                    }
                }
            }
            case SECTOR_NORTH -> {
                switch (bound) {
                    case W -> {
                        serverToConnectName = Server.SECTOR_WEST;
                        serializableLocation.setX(serializableLocation.getX() - 2);
                    }
                    case E, N -> {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Dotarłeś do granicy mapy!"));
                        return;
                    }
                    case S -> {
                        if (playerX <= 75)
                            serverToConnectName = Server.SPAWN;
                        else
                            serverToConnectName = Server.SECTOR_EAST;
                        serializableLocation.setZ(serializableLocation.getZ() + 2);
                    }
                }
            }
            case SECTOR_EAST -> {
                switch (bound) {
                    case W -> {
                        if (playerZ <= 75)
                            serverToConnectName = Server.SPAWN;
                        else
                            serverToConnectName = Server.SECTOR_SOUTH;
                        serializableLocation.setX(serializableLocation.getX() - 2);
                    }
                    case E, S -> {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Dotarłeś do granicy mapy!"));
                        return;
                    }
                    case N -> {
                        serverToConnectName = Server.SECTOR_NORTH;
                        serializableLocation.setZ(serializableLocation.getZ() - 2);
                    }
                }
            }
        }

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        try {
            out.writeUTF("Connect");
            out.writeUTF(serverToConnectName.toString().toLowerCase());
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.sendPluginMessage(Sectors.getPlugin(Sectors.class), "changesector:main", b.toByteArray());
        RedisPubSubSystem.publish(serializableLocation);
    }

    private Optional<Bound> getBound(Location location) {

        if (location.getBlockX() >= boundE) {
            return Optional.of(Bound.E);

        } else if (location.getBlockX() <= boundW) {
            return Optional.of(Bound.W);

        } else if (location.getBlockZ() >= boundS) {
            return Optional.of(Bound.S);

        } else if (location.getBlockZ() <= boundN) {
            return Optional.of(Bound.N);

        }

        return Optional.empty();
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        ByteArrayDataInput byteArrayInput = ByteStreams.newDataInput(message);
        String s1 = byteArrayInput.readUTF();
        String s2 = byteArrayInput.readUTF();
        player.sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(byteArrayInput.readUTF()));
        System.out.println(s1);
        System.out.println(s2);
        System.out.println(byteArrayInput.readUTF());
    }
}
