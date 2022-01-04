package dev.polychroma.configeditor;

import dev.polychroma.configeditor.command.ConfigEditCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class Configeditor extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        ConfigEditCommand configEditCommand = new ConfigEditCommand(this);
        getCommand("editconfig").setExecutor(configEditCommand);
        getCommand("editconfig").setTabCompleter(configEditCommand);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
