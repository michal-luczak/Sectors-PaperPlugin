package me.taison.sectors;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static me.taison.sectors.Bound.getBound;
import static me.taison.sectors.Bound.isOutOfBound;

public class Teleport {

    public Teleport(Player player) {
        if (!isOutOfBound(player.getLocation()) && getBound(player.getLocation()).isEmpty())
            return;

        int playerX = player.getLocation().getBlockX();
        int playerZ = player.getLocation().getBlockZ();

        Server serverName = Server.valueOf(Sectors.getPlugin(Sectors.class).getConfig().getString("server_name").toUpperCase());

        Server serverToConnectName = Server.SPAWN;
        PlayerDataToTransfer playerDataToTransfer = new PlayerDataToTransfer(player, Bukkit.getServer().getPort());
        Bound bound = getBound(player.getLocation()).get();

        switch (serverName) {
            case SPAWN -> {
                switch (bound) {
                    case W -> {
                        serverToConnectName = Server.SECTOR_WEST;
                        playerDataToTransfer.setX(playerDataToTransfer.getX() - 1);
                    }
                    case E -> {
                        serverToConnectName = Server.SECTOR_EAST;
                        playerDataToTransfer.setX(playerDataToTransfer.getX() + 1);
                    }
                    case S -> {
                        serverToConnectName = Server.SECTOR_SOUTH;
                        playerDataToTransfer.setZ(playerDataToTransfer.getZ() + 1);
                    }
                    case N -> {
                        serverToConnectName = Server.SECTOR_NORTH;
                        playerDataToTransfer.setZ(playerDataToTransfer.getZ() - 1);
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
                        playerDataToTransfer.setX(playerDataToTransfer.getX() + 1);
                    }
                    case S -> {
                        serverToConnectName = Server.SECTOR_SOUTH;
                        playerDataToTransfer.setZ(playerDataToTransfer.getZ() + 1);
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
                        playerDataToTransfer.setX(playerDataToTransfer.getX() + 1);
                    }
                    case N -> {
                        if (playerX >= -75)
                            serverToConnectName = Server.SPAWN;
                        else
                            serverToConnectName = Server.SECTOR_WEST;
                        playerDataToTransfer.setZ(playerDataToTransfer.getZ() - 1);
                    }
                }
            }
            case SECTOR_NORTH -> {
                switch (bound) {
                    case W -> {
                        serverToConnectName = Server.SECTOR_WEST;
                        playerDataToTransfer.setX(playerDataToTransfer.getX() - 1);
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
                        playerDataToTransfer.setZ(playerDataToTransfer.getZ() + 1);
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
                        playerDataToTransfer.setX(playerDataToTransfer.getX() - 1);
                    }
                    case E, S -> {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Dotarłeś do granicy mapy!"));
                        return;
                    }
                    case N -> {
                        serverToConnectName = Server.SECTOR_NORTH;
                        playerDataToTransfer.setZ(playerDataToTransfer.getZ() - 1);
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

        if (isOutOfBound(player.getLocation())) {
            playerDataToTransfer.setX(playerX);
            playerDataToTransfer.setZ(playerZ);
            playerDataToTransfer.setY(player.getLocation().getBlockY());
            player.sendPluginMessage(Sectors.getPlugin(Sectors.class), "changesector:force", b.toByteArray());
        } else
            player.sendPluginMessage(Sectors.getPlugin(Sectors.class), "changesector:main", b.toByteArray());

        new RedisPubSubSystem().publish(playerDataToTransfer, (byte) 9);
    }
}
