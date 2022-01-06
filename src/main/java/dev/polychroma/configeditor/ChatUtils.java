package dev.polychroma.configeditor;

import net.md_5.bungee.api.ChatColor;

import java.awt.*;

public class ChatUtils {

    public static final ChatColor MAIN_COLOR = ChatColor.of(new Color(18, 183, 61, 255));
    public static final ChatColor SECONDARY_COLOR = ChatColor.of(new Color(12, 197, 163));

    public static final ChatColor MESSAGE_COLOR = ChatColor.of(new Color(181, 234, 229));
    public static final ChatColor MESSAGE_FAIL_COLOR = ChatColor.of(new Color(210, 24, 24));
    public static final ChatColor MESSAGE_SUCCESS_COLOR = ChatColor.of(new Color(27, 136, 13));

    public static final String PREFIX = MAIN_COLOR + "[Config Editor]";
}
