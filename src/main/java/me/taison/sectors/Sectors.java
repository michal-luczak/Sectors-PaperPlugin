package me.taison.sectors;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Sectors extends JavaPlugin {

    public static int boundW;
    public static int boundE;
    public static int boundS;
    public static int boundN;

    private final static Thread thread = new Thread(new RedisPubSubSystem()::subscribeChannel);

    @Override
    public void onEnable() {
        boundW = Sectors.getPlugin(Sectors.class).getConfig().getInt("bound_W");
        boundE = Sectors.getPlugin(Sectors.class).getConfig().getInt("bound_E");
        boundS = Sectors.getPlugin(Sectors.class).getConfig().getInt("bound_S");
        boundN = Sectors.getPlugin(Sectors.class).getConfig().getInt("bound_N");
        getServer().getPluginManager().registerEvents(new WorldBorderBoundsEvent(), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "changesector:main");
        getServer().getMessenger().registerOutgoingPluginChannel(this, "changesector:force");
        saveDefaultConfig();
        thread.start();
    }

    @Override
    public void onDisable() {
        thread.interrupt();
    }

    public static void teleport(Player player) {
        new Teleport(player);
    }
}
