package com.kbparty;

import org.bukkit.plugin.java.JavaPlugin;

public class WhackABat extends JavaPlugin {

    private WhackABatListener listener;

    @Override
    public void onEnable() {
        getLogger().info("WhackABat loaded! Happy Birthday Kara! 🎂");
        listener = new WhackABatListener(this);
        getServer().getPluginManager().registerEvents(listener, this);
        getCommand("whackabat").setExecutor(new WhackABatCommand(this, listener));
    }

    @Override
    public void onDisable() {
        getLogger().info("WhackABat disabled.");
    }

    public WhackABatListener getListener() {
        return listener;
    }
}