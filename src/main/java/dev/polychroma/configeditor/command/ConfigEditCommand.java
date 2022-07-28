package dev.polychroma.configeditor.command;

import dev.polychroma.configeditor.ChatUtils;
import dev.polychroma.configeditor.Configeditor;
import dev.polychroma.configeditor.Pair;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConfigEditCommand implements TabExecutor {

    private final HashMap<String, Pair<File, YamlConfiguration>> configFiles = new HashMap<>();

    private final PluginManager pluginManager;

    public ConfigEditCommand(Configeditor configeditor) {
        pluginManager = configeditor.getServer().getPluginManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("config.edit")) {
            sender.sendMessage(ChatUtils.MESSAGE_FAIL_COLOR + "You do not have permission to use this command.");
        }
        if (args.length < 1) {
            sender.sendMessage(getHelpMessage());
            return true;
        }
        switch (args[0]) {
            case "load" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatUtils.PREFIX + " " + ChatUtils.MESSAGE_FAIL_COLOR + "Invalid syntax!");
                    sender.sendMessage(getloadSyntax());
                    return true;
                }
                if (args.length == 2) {
                    onLoadCommand(args[1], sender, null);
                } else {
                    onLoadCommand(args[1], sender, args[2]);
                }
                return true;
            }
            case "read" -> {
                if (args.length < 3) {
                    sender.sendMessage(ChatUtils.PREFIX + " " + ChatUtils.MESSAGE_FAIL_COLOR + "Invalid syntax!");
                    sender.sendMessage(getReadSyntax());
                    return true;
                }
                onReadCommand(args[1], sender, args[2]);
                return true;
            }
            case "set" -> {
                if (args.length < 4) {
                    sender.sendMessage(ChatUtils.PREFIX + " " + ChatUtils.MESSAGE_FAIL_COLOR + "Invalid syntax!");
                    sender.sendMessage(getSetSyntax());
                    return true;
                }
                onSetCommand(args[1], sender, args[2], args[3]);
                return true;
            }
            case "save" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatUtils.PREFIX + " " + ChatUtils.MESSAGE_FAIL_COLOR + "Invalid syntax!");
                    sender.sendMessage(getSaveSyntax());
                    return true;
                }
                onSaveCommand(args[1], sender);
                return true;
            }
            case "print" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatUtils.PREFIX + " " + ChatUtils.MESSAGE_FAIL_COLOR + "Invalid syntax!");
                    sender.sendMessage(getPrintSyntax());
                    return true;
                }
                String pg;
                if (args.length >= 3) {
                    pg = args[2];
                } else {
                    pg = "1";
                }
                try {
                    int page = Integer.parseInt(pg);
                    onPrintCommand(args[1], sender, page);
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatUtils.PREFIX + " " + ChatUtils.MESSAGE_FAIL_COLOR + "Invalid Syntax!");
                    sender.sendMessage(getPrintSyntax());
                }
                return true;
            }
            default -> {
                sender.sendMessage(getHelpMessage());
                return true;
            }
        }
    }

    /**
     * Loads the config file
     *
     * @param plugin   Plugin that owns the file, used to get the right directory
     * @param sender   CommandSender who sent the command
     * @param fileName name of the file to load. If it is null it will use the default of "config.yml"
     */
    private void onLoadCommand(String plugin, CommandSender sender, @Nullable String fileName) {
        Plugin targetPlugin = pluginManager.getPlugin(plugin);
        if (targetPlugin == null) {
            sender.sendMessage(ChatUtils.PREFIX + " " + ChatUtils.MESSAGE_FAIL_COLOR + "Unable to find plugin " + plugin + ".");
            return;
        }

        File targetDir = targetPlugin.getDataFolder();

        //Set file name to default name if null
        fileName = Objects.requireNonNullElse(fileName, "config.yml");
        File targetFile = new File(targetDir, fileName);

        if (!targetFile.exists()) {
            sender.sendMessage(ChatUtils.PREFIX + " " + ChatUtils.MESSAGE_FAIL_COLOR + "Unable to find config file \"" + fileName + "\" for plugin " + plugin + ".");
            return;
        }

        YamlConfiguration targetConfig = YamlConfiguration.loadConfiguration(targetFile);
        configFiles.put(plugin, new Pair<>(targetFile, targetConfig));

        sender.sendMessage(ChatUtils.PREFIX + " " + ChatUtils.MESSAGE_SUCCESS_COLOR + "Config file for " + plugin + " has been loaded.");
    }

    private void onReadCommand(String plugin, CommandSender sender, @Nonnull String field) {
        if (!configFiles.containsKey(plugin)) {
            sender.sendMessage(ChatUtils.MESSAGE_FAIL_COLOR + "Config file for " + plugin + " has not been loaded. Please use the /config load command first");
            return;
        }

        YamlConfiguration configuration = configFiles.get(plugin).getRight();

        Object o = configuration.get(field);

        sender.sendMessage(ChatUtils.PREFIX + " " + ChatUtils.MESSAGE_SUCCESS_COLOR + "The value for " + field + " is");
        //TODO: Might have to change what is displayed depending on what value type is saved
        if (o == null) {
            sender.sendMessage(ChatUtils.SECONDARY_COLOR + "NULL");
        } else {
            sender.sendMessage(ChatUtils.SECONDARY_COLOR + o.toString());
        }
    }

    private void onSetCommand(String plugin, CommandSender sender, String field, String value) {
        if (!configFiles.containsKey(plugin)) {
            sender.sendMessage(ChatUtils.PREFIX + " " + ChatUtils.MESSAGE_FAIL_COLOR + "Config file for " + plugin + " has not been loaded. Please use the /config load command first.");
            return;
        }

        YamlConfiguration configuration = configFiles.get(plugin).getRight();
        //TODO: Change how value is set depending on current value type
        configuration.set(field, value);

        sender.sendMessage(ChatUtils.PREFIX + " " + ChatUtils.MESSAGE_SUCCESS_COLOR + "Set the value of " + field + " to " + ChatUtils.SECONDARY_COLOR + value + ChatUtils.MESSAGE_SUCCESS_COLOR + ".");
    }

    private void onSaveCommand(String plugin, CommandSender sender) {
        if (!configFiles.containsKey(plugin)) {
            sender.sendMessage(ChatUtils.PREFIX + " " + ChatUtils.MESSAGE_FAIL_COLOR + "Config file for " + plugin + " has not been loaded. Please use the /config load command first.");
            return;
        }

        YamlConfiguration configuration = configFiles.get(plugin).getRight();
        File file = configFiles.get(plugin).getLeft();
        try {
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage(ChatUtils.PREFIX + " " + ChatUtils.MESSAGE_FAIL_COLOR + "Unable to save configuration file!");
        }

        sender.sendMessage(ChatUtils.PREFIX + " " + ChatUtils.MESSAGE_SUCCESS_COLOR + "Changes saved to file. " + plugin + " will need to be reloaded before changes are applied.");
    }

    private void onPrintCommand(String plugin, CommandSender sender, int page) {
        if (!configFiles.containsKey(plugin)) {
            sender.sendMessage(ChatUtils.PREFIX + " " + ChatUtils.MESSAGE_FAIL_COLOR + "Config file for " + plugin + " has not been loaded. Please use the /config load command first.");
            return;
        }

        YamlConfiguration config = configFiles.get(plugin).getRight();

        //TODO: Fix ugly loop
        int count = 0;
        for (String key : config.getKeys(true)) {
            if (count < (page - 1) * 15) {
                count++;
                continue;
            }
            if (count > page * 15) {
                break;
            }
            sender.sendMessage(ChatUtils.MAIN_COLOR + translateKey(key) + ChatUtils.MESSAGE_COLOR + " - " + ChatUtils.SECONDARY_COLOR + config.get(key));
            count++;
        }

        sender.sendMessage(ChatUtils.PREFIX + " " + ChatUtils.MESSAGE_COLOR + "To view the next page use /config print " + plugin + " " + ++page);
    }

    private String translateKey(String key) {
        if (key.contains(".")) {
            //Capture 0 or more spaces then replace up to . including .
            Pattern p = Pattern.compile("[^.┕━]+\\.");
            Matcher m = p.matcher(key);
            m = p.matcher(m.replaceFirst(ChatUtils.MESSAGE_COLOR + "┕━━━" + ChatUtils.MAIN_COLOR));
            return m.replaceAll(ChatUtils.MESSAGE_COLOR + "━━━━" + ChatUtils.MAIN_COLOR);
        }
        return key;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length < 1) {
            return Collections.emptyList();
        }
        //sub-command
        if (args.length == 1) {
            return getSubCommands().stream()
                    .filter(s -> s.startsWith(args[0]))
                    .collect(Collectors.toList());
        }
        //plugin name
        if (args.length == 2) {
            return getPluginNames().stream()
                    .filter(s -> s.startsWith(args[1]))
                    .collect(Collectors.toList());
        }
        //Field name
        if (args.length == 3 && !args[0].equals("print")) {
            return getFieldNames(args[1], args[2]);
        }
        return Collections.emptyList();
    }

    private List<String> getSubCommands() {
        ArrayList<String> ret = new ArrayList<>();

        ret.add("load");
        ret.add("read");
        ret.add("set");
        ret.add("save");
        ret.add("print");

        return ret;
    }

    private List<String> getPluginNames() {
        Plugin[] plugins = pluginManager.getPlugins();
        return Arrays.stream(plugins)
                .map(Plugin::getName)
                .collect(Collectors.toList());
    }

    private List<String> getFieldNames(String plugin, String depth) {
        if (!configFiles.containsKey(plugin)) {
            return null;
        }
        YamlConfiguration config = configFiles.get(plugin).getRight();

        Set<String> possibleKeys = config.getKeys(true);

        return possibleKeys.stream()
                .filter(s -> s.startsWith(depth))
                .collect(Collectors.toList());
    }

    private String getHelpMessage() {
        return ChatUtils.PREFIX + ChatUtils.MESSAGE_COLOR + " - A plugin by Safyre" + "\n" +
                "    " + getloadSyntax() + ChatUtils.MESSAGE_COLOR + " - " + getLoadDescription() + "\n" +
                "    " + getReadSyntax() + ChatUtils.MESSAGE_COLOR + " - " + getReadDescription() + "\n" +
                "    " + getSetSyntax() + ChatUtils.MESSAGE_COLOR + " - " + getSetDescription() + "\n" +
                "    " + getSaveSyntax() + ChatUtils.MESSAGE_COLOR + " - " + getSaveDescription() + "\n" +
                "    " + getPrintSyntax() + ChatUtils.MESSAGE_COLOR + " - " + getPrintDescription() + "\n";
    }

    private String getloadSyntax() {
        return ChatUtils.MESSAGE_COLOR + "/config load " + ChatUtils.MAIN_COLOR + "<plugin name> " + ChatUtils.SECONDARY_COLOR + "[file name]";
    }

    private String getLoadDescription() {
        return ChatUtils.MESSAGE_COLOR +
                "Loads the config for a plugin. Optionally you can specify the file name if it differs from" +
                ChatUtils.MAIN_COLOR + " \"config.yml\"" + ChatUtils.MESSAGE_COLOR + ".";
    }

    private String getReadSyntax() {
        return ChatUtils.MESSAGE_COLOR + "/config read " + ChatUtils.MAIN_COLOR + "<plugin name> <property key>";
    }

    private String getReadDescription() {
        return ChatUtils.MESSAGE_COLOR + "Reads a value from the config.";
    }

    private String getSetSyntax() {
        return ChatUtils.MESSAGE_COLOR + "/config set " + ChatUtils.MAIN_COLOR + "<plugin name> <property key> <value>";
    }

    private String getSetDescription() {
        return ChatUtils.MESSAGE_COLOR + "Sets a value in the config.";
    }

    private String getSaveSyntax() {
        return ChatUtils.MESSAGE_COLOR + "/config save " + ChatUtils.MAIN_COLOR + "<plugin name>";
    }

    private String getSaveDescription() {
        return ChatUtils.MESSAGE_COLOR + "Saves the changes from the config to the original file.";
    }

    private String getPrintSyntax() {
        return ChatUtils.MESSAGE_COLOR + "/config print " + ChatUtils.MAIN_COLOR + "<plugin name> " + ChatUtils.SECONDARY_COLOR + "[page number]";
    }

    private String getPrintDescription() {
        return ChatUtils.MESSAGE_COLOR + "Prints the contents of the config in your chat. If the page number is given it will display that page, " +
                "otherwise it will display the first page";
    }

}
