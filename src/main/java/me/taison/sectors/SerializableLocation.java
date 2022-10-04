package me.taison.sectors;

import org.bukkit.Location;

import java.io.Serializable;
import java.util.UUID;

public class SerializableLocation implements Serializable {

    private int x,y,z;
    private UUID uuid;
    private int serverPort;

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

    public SerializableLocation(Location location, UUID uuid, int serverPort) {
        x = location.getBlockX();
        y = location.getBlockY();
        z = location.getBlockZ();
        this.uuid = uuid;
        this.serverPort = serverPort;
        yaw = location.getYaw();
    }
}
