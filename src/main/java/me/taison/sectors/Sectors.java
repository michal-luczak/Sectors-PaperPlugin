package me.taison.sectors;

import org.bukkit.plugin.java.JavaPlugin;

public final class Sectors extends JavaPlugin {

    private final static Thread thread = new Thread(RedisPubSubSystem::subscribeChannel);

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new WorldBorderBoundsEvent(), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "changesector:main");
        getServer().getMessenger().registerIncomingPluginChannel(this, "changesector:main", new WorldBorderBoundsEvent());
        saveDefaultConfig();

        thread.start();
    }



    @Override
    public void onDisable() {
        thread.stop();
    }
}
