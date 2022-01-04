package dev.polychroma.configeditor.command;

import dev.polychroma.configeditor.Configeditor;
import dev.polychroma.configeditor.Pair;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

//TODO: Colored messages
public class ConfigEditCommand implements TabExecutor {

    private final HashMap<String, Pair<File, YamlConfiguration>> configFiles = new HashMap<>();

    private final PluginManager pluginManager;

    public ConfigEditCommand(Configeditor configeditor){
        pluginManager = configeditor.getServer().getPluginManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length < 1){
            //TODO: Replace with help message
            sender.sendMessage("Please specify sub command");
            return true;
        }
        switch (args[0]){
            case "load":
                if(args.length < 2){
                    //TODO: Replace with command-specific help message
                    sender.sendMessage("Please specify which plugin you'd like to load the config of");
                    return true;
                }
                onLoadCommand(args[1], sender, args[2]);
                return true;
            case "read":
                if(args.length < 3){
                    //TODO: Replace with command-specific help message
                    sender.sendMessage("Please specify plugin and property name");
                    return true;
                }
                onReadCommand(args[1], sender, args[2]);
                return true;
            case "set":
                if(args.length < 4){
                    //TODO: Replace with command-specific help message
                    sender.sendMessage("Please specify plugin, property name, and value");
                    return true;
                }
                onSetCommand(args[1], sender, args[2], args[3]);
                return true;
            default:
                //TODO: Replace with help message
                sender.sendMessage("Sub command not recognized");
                return false;
        }
    }

    /**
     * Loads the config file
     * @param plugin Plugin that owns the file, used to get the right directory
     * @param sender CommandSender who sent the command
     * @param fileName name of the file to load. If it is null it will use the default of "config.yml"
     */
    private void onLoadCommand(String plugin, CommandSender sender, @Nullable String fileName){
        Plugin targetPlugin = pluginManager.getPlugin(plugin);
        if(targetPlugin == null){
            sender.sendMessage("Unable to find plugin " + plugin);
            return;
        }

        File targetDir = targetPlugin.getDataFolder();

        //Set file name to default name if null
        fileName = Objects.requireNonNullElse(fileName, "config.yml");
        File targetFile = new File(targetDir, fileName);

        if(!targetFile.exists()){
            sender.sendMessage("Unable to find config file \"" + fileName + "\" for plugin " + plugin);
            return;
        }

        YamlConfiguration targetConfig = YamlConfiguration.loadConfiguration(targetFile);
        configFiles.put(plugin, new Pair<>(targetFile, targetConfig));

        sender.sendMessage("Config file for " + plugin + " has been loaded. You can now edit it with /config edit " + plugin);
    }

    private void onReadCommand(String plugin, CommandSender sender, String field){
        if(!configFiles.containsKey(plugin)){
            sender.sendMessage("Config file for " + plugin + " has not been loaded. Please use the /config load command first");
            return;
        }

        YamlConfiguration configuration = configFiles.get(plugin).getRight();

        Object o = configuration.get(field);

        sender.sendMessage("The value for " + field + " is");
        //TODO: Might have to change what is displayed depending on what value type is saved
        if(o == null){
            sender.sendMessage("NULL");
        } else {
            sender.sendMessage(o.toString());
        }
    }

    private void onSetCommand(String plugin, CommandSender sender, String field, String value){
        if(!configFiles.containsKey(plugin)){
            sender.sendMessage("Config file for " + plugin + " has not been loaded. Please use the /config load command first");
            return;
        }

        YamlConfiguration configuration = configFiles.get(plugin).getRight();
        //TODO: Change how value is set depending on current value type
        configuration.set(field, value);

        sender.sendMessage("Set the value of " + field + " to " + value);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(args.length < 1){
            return null;
        }
        //sub-command
        if (args.length == 1){
            return getSubCommands().stream()
                    .filter(s -> s.startsWith(args[0]))
                    .collect(Collectors.toList());
        }
        //plugin name
        if (args.length == 2){
            return getPluginNames().stream()
                    .filter(s -> s.startsWith(args[1]))
                    .collect(Collectors.toList());
        }
        //Field name
        if (args.length == 3){
            return getFieldNames(args[1], args[2]);
        }
        return null;
    }

    private List<String> getSubCommands(){
        ArrayList<String> ret = new ArrayList<>();

        ret.add("load");
        ret.add("read");
        ret.add("set");

        return ret;
    }

    private List<String> getPluginNames(){
        Plugin[] plugins = pluginManager.getPlugins();
        return Arrays.stream(plugins)
                .map(Plugin::getName)
                .collect(Collectors.toList());
    }

    private List<String> getFieldNames(String plugin, String depth){
        YamlConfiguration config = configFiles.get(plugin).getRight();

        Set<String> possibleKeys = config.getKeys(true);

        return possibleKeys.stream()
                .filter(s -> s.startsWith(depth))
                .collect(Collectors.toList());
    }

}
